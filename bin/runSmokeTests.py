import sys
import subprocess
import copy

command = copy.copy(sys.argv)
command[0] = './runSmokeTests.sh';

subprocess.call(command)

