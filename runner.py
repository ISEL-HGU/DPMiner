import shutil
import sys
import os
import subprocess
import getopt
import zipfile

def build() -> bool:
    print("[debug] > build")
    try:
        jdk = os.environ.copy()
        jdk['JAVA_HOME'] = "/usr/lib/jvm/java-11-openjdk-amd64/"
        assert subprocess.run(["./gradlew", "clean", "build"], cwd=".", shell=True, env = jdk)
    except Exception as e:
        print("[debug] > build error : ",e)
        return False
    return True

def unzip(file, destination) -> bool:
    try:
        with zipfile.ZipFile(file, 'r') as zip_ref:
            zip_ref.extractall(destination)
    except Exception as e:
        print("[debug] > unzip error : ",e)
        return False
    return True

def run_application(url, output_path, mode, key) -> bool:
    print(f"[debug] > running application on [{url}, {output_path}, {mode}, {key}]")
    try:
        os.chmod("./build/DPMiner/bin/DPMiner", 0o774)
        assert subprocess.run(["./build/DPMiner/bin/DPMiner", mode, "-i", url, "-ij", f"-jk={key}", "-o", output_path], cwd=".")
    except Exception as e:
        print("[debug] > run error : ",e)
        return False
    return True

def purge() -> bool:
    print("[debug] > purge")
    try:
        shutil.rmtree("build/")
    except Exception as e:
        print("[debug] > purge error : ",e)
        return False
    return True

def main(argv):
    print("[debug] > initiate")
    try:
        print("[debug] > getting argv...")
        opts, args = getopt.getopt(argv[1:], "u:o:k:r:c:m:", ["url", "output", "key", "rebuild", "clean", "mode"])
    except getopt.GetoptError as err:
        print("[error] > error on GetOpt : ",err)
        sys.exit(2)
    except Exception as e:
        print("[error] > error on GetOpt : ",e)
        sys.exit(2)

    url = ''
    output_path = ''
    key = ''
    rebuild = False
    mode = ''

    for o, a in opts:
        if o in ("-u", "--url"):
            url = a
            print(f"[debug] > url = {url}")
        elif o in ("-k", "--key"):
            key = a
            print(f"[debug] > key = {key}")
        elif o in ("-o", "--output"):
            output_path = a
            print(f"[debug] > output_path = {output_path}")
        elif o in ("-r", "--rebuild"):
            rebuild = True
            print(f"[debug] > rebuild = {rebuild}")
        elif o in ("-c", "--clean"):
            purge()
        elif o in ("-m", "--mode"):
            mode = a
            print(f"[debug] > mode = {mode}")
        else:
            assert False, "unhandled option"
    
    # rebuild if needed
    if rebuild:
        if not build():
            print("[debug] > build failed")
            return
        if not unzip("build/distributions/DPMiner.zip", "build/"):
            print("[debug] > unzip failed")
            return

    if not run_application(url, output_path, mode, key):
        print("[debug] > run application failed")
        return
        
    print("[debug] > done")

if __name__ == '__main__':
    main(sys.argv)