import shutil
import sys
import os
import zipfile
import subprocess
import traceback
import argparse
import configparser
import jproperties
import datetime as dt
import csv

def parse_argv() -> tuple:
    parser = argparse.ArgumentParser()
    args = parser.parse_args()
    settings = configparser.ConfigParser()
    settings.optionxform = str
    settings.read(args.config)
    cases = list()
    return (cases, settings)

def run() -> bool:
    try:
        jdk8_env = os.environ.copy()
        jdk8_env['JAVA_HOME'] = "/home/codemodel/hans/paths/jdk1.8.0_311/"
        assert subprocess.run(["./gradlew", "clean", "build"], cwd=".", shell=True, env = jdk8_env)
    except Exception as e:
        print(e)
        traceback.print_exc()
        return False
    return True

def main(argv):
    if run() == True:
        print("Success")
    else:
        print("Failure")

if __name__ == '__main__':
    main(sys.argv)