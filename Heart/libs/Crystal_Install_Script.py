from __future__ import absolute_import, division, print_function, unicode_literals

import os
import platform
import sys
from pathlib import Path
from zipfile import ZipFile

devBuild = False
launchAfter = False

for arg in sys.argv:
    if arg == '-dev':
        devBuild = True
    if arg == '-launch':
        launchAfter = True

print("Crystal Home Systems Install Script")

print("Ensuring working directory integrity...")
# If /CrystalHomeSys/ exists, continue. If not, create it
userhome = str(Path.home())
crystalpath = userhome + "/CrystalHomeSys"
if os.path.isdir(crystalpath):
    print("Working directory found.")
else:
    print("Working directory not found. Creating...")
    os.makedirs(userhome + "/CrystalHomeSys")
    print("Working directory created.")


def installDependencies():
    import pip
    retcode = 0
    pipcode = pip.main(['install', 'zeroconf'])
    retcode = retcode or pipcode
    return retcode


print("Installing dependencies...")
if installDependencies() == 1:
    print("Dependencies require Visual C++ 14 or above. Please install those first.")
    sys.exit(1)
else:
    print("Dependencies installed.")

# def searchForService():
#     import logging
#     import socket
#     import sys
#     from time import sleep
#     from zeroconf import ServiceBrowser, ServiceStateChange, Zeroconf
#     def on_service_state_change(zeroconf, service_type, name, state_change):
#         print("Service %s of type %s state changed: %s" % (name, service_type, state_change))
#
#         if state_change is ServiceStateChange.Added:
#             info = zeroconf.get_service_info(service_type, name)
#             if info:
#                 print("  Address: %s:%d" % (socket.inet_ntoa(info.address), info.port))
#                 print("  Weight: %d, priority: %d" % (info.weight, info.priority))
#                 print("  Server: %s" % (info.server,))
#                 if info.properties:
#                     print("  Properties are:")
#                     for key, value in info.properties.items():
#                         print("    %s: %s" % (key, value))
#                 else:
#                     print("  No properties")
#             else:
#                 print("  No info")
#             print('\n')
#
#     if __name__ == '__main__':
#         logging.basicConfig(level=logging.DEBUG)
#         if len(sys.argv) > 1:
#             assert sys.argv[1:] == ['--debug']
#             logging.getLogger('zeroconf').setLevel(logging.DEBUG)
#
#         zeroconf = Zeroconf()
#         print("\nBrowsing services, press Ctrl-C to exit...\n")
#         browser = ServiceBrowser(zeroconf, "_http._tcp.local.", handlers=[on_service_state_change])
#
#         try:
#             while True:
#                 sleep(0.1)
#         except KeyboardInterrupt:
#             pass
#         finally:
#             zeroconf.close()


print("Searching for an existing Heart server...")
# Search for Heart DNSSD service
if False:
    print("Found existing Heart server! Creating Shard install...")
    # Create working Shard directory
    if not os.path.isdir(userhome + "/CrystalHomeSys/Shard"):
        os.makedirs(userhome + "/CrystalHomeSys/Shard")

    print("Downloading Shard files from Heart server...")
    # Launch .jar updater file to pull shard from heart
else:
    print("No existing Heart server found! Creating Heart install...")
    # Create working Heart directory
    # if not os.path.isdir(userhome + "/CrystalHomeSys/Heart"):
    #     os.makedirs(userhome + "/CrystalHomeSys/Heart")

    print("Downloading Crystal Home Systems from repository...")
    # Download and unzip files without saving to hdd
    from io import BytesIO
    from urllib.request import urlopen

    if devBuild:
        # Dev
        gitAddress = "https://github.com/PulsePanda/Crystal/archive/dev.zip"
    else:
        # Master
        gitAddress = "https://github.com/PulsePanda/Crystal/archive/master.zip"

    zipurl = gitAddress
    with urlopen(zipurl) as zipresp:
        with ZipFile(BytesIO(zipresp.read())) as zfile:
            zfile.extractall(userhome + "/CrystalHomeSys/")

    dir_src = userhome + "\CrystalHomeSys\Crystal-"
    if devBuild:
        dir_src = dir_src + "dev"
    else:
        dir_src = dir_src + "master"

    print("Compiling files...")
    from subprocess import Popen

    p = Popen("gradlew build", shell=True, cwd=dir_src)
    stdout, stderr = p.communicate()

    # finish assigning dir_src to the sub Heart directory for copy
    dir_src_nonspecific = dir_src
    dir_src = dir_src + "/Heart"

    print("Updating Heart files...")
    dir_dst = userhome + "/CrystalHomeSys/"
    # import distutils
    #
    # distutils.dir_util.copy_tree(dir_src, dir_dst)
    zip_ref = ZipFile(dir_src + "/build/distributions/Heart.zip")
    zip_ref.extractall(dir_dst)
    zip_ref.close()

    print("Packaging Shard install for distribution...")
    patchHome = dir_dst + "patch"
    if not os.path.isdir(patchHome):
        os.makedirs(patchHome)
    os.replace(dir_src_nonspecific + "/Shard/build/distributions/Shard.zip", patchHome + "/Shard.zip")

    print("Cleaning up...")
    import shutil

    shutil.rmtree(dir_src_nonspecific)

    if launchAfter:
        print("Starting Heart server...")
        if "Linux" in platform.system():
            Popen("Heart", shell=True, cwd=userhome + "/CrystalHomeSys/Heart/bin/")
        elif "Windows" in platform.system():
            Popen("Heart.bat", shell=True, cwd=userhome + "/CrystalHomeSys/Heart/bin/")

    print("-------------Application Launch Directory: " + dir_dst + "Heart/bin/Heart.(sh/bat)-------------")

sys.exit(0)
