import os
import subprocess
import sys
import threading

from concurrent.futures import ProcessPoolExecutor, as_completed
from datetime import datetime
from pathlib import Path
from tqdm import tqdm


csv_lock = threading.Lock()
log_lock = threading.Lock()

mydir = os.path.dirname(os.path.abspath(__file__))
outdir = os.path.join(mydir, "data", datetime.now().strftime('%Y-%m-%d-%H-%M-%S'))
logfile = os.path.join(outdir, "err.log")
os.makedirs(outdir, exist_ok=True)

num_jobs = 4
timeout = 60

def print_csv_header(outfile):
    with csv_lock:
        with open(outfile, 'w') as f:
            f.write("name,inputs,size,threshold,threshold_param,threshold_cross,index,size_trim,size_bisim,size_sim,sim_rels,size_sc1,size_sc1_min,size_sc2,size_sc2_min,max_inter,time_bisim,time_sim,time_sc1,time_sc1_min,time_sc2,time_sc2_min,time_total,cancel\n")

def run_benchmark(cmd, infile, outfile):
    with (subprocess.Popen(cmd + [infile], stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True) as p):
        try:
            out, err = p.communicate(timeout = timeout)

            retcode = p.poll()

            if retcode == 0:
                with csv_lock:
                    with open(outfile, 'a') as out_f:
                        out_f.write(f"{out}\n")
            else:
                with csv_lock:
                    with open(outfile, 'a') as out_f:
                        out_f.write(f"{os.path.basename(infile)},ERR\n")
                with log_lock:
                    with open(logfile, 'a') as out_f:
                        out_f.write(f"######\n{os.path.abspath(infile)}\n{cmd}\n{out}\n{err}\n")
        except subprocess.TimeoutExpired as ex:
            p.terminate()

            try:
                out, _ = p.communicate(timeout = 60)
                with csv_lock:
                    with open(outfile, 'a') as out_f:
                        out_f.write(f"{out}TO\n")
            except subprocess.TimeoutExpired:
                p.kill()

def main(directory):
    benchdir = os.path.abspath(directory)
    systems = [os.path.join(benchdir, f) for f in Path(benchdir).glob( '**/*[.mata|.ba]' )]
    jar = os.path.join(mydir, "build/libs/otf-benchmark.jar")
    java_opts = os.environ.get('JAVA_OPTS')

    if java_opts is None:
        cmdl = ["java", "-jar", jar]
    else:
        cmdl = ["java"] + java_opts.split() + ["-jar", jar]

    benchmarks = {
        "sc": cmdl + ["-m=SC"],
        "sc-s": cmdl + ["-m=SC_S"],
        "brz": cmdl + ["-m=BRZ"],
        "brz-s": cmdl + ["-m=BRZ_S"],
        "otf": cmdl + ["-m=OTF"],
        "otf-s": cmdl + ["-m=OTF_S"],
        "brz-otf": cmdl + ["-m=BRZ_OTF"],
        "brz-otf-s": cmdl + ["-m=BRZ_OTF_S"],
    }

    total_files = len(systems) * len(benchmarks)

    with ProcessPoolExecutor(max_workers=num_jobs) as executor:

        futures = []

        for benchmark_name, cmd in benchmarks.items():
            outfile = os.path.join(outdir, f"{benchmark_name}.csv")
            print_csv_header(outfile)

            for system_file in systems:
                futures.append(executor.submit(run_benchmark, cmd, system_file, outfile))

        for future in tqdm(as_completed(futures), total=total_files):
            future.result()


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python script.py <directory> [timeout] [number_of_jobs]")
        sys.exit(1)

    directory_arg = sys.argv[1]

    if len(sys.argv) >= 3:
        timeout = int(sys.argv[2])

    if len(sys.argv) >= 4:
        num_jobs = int(sys.argv[3])

    main(directory_arg)