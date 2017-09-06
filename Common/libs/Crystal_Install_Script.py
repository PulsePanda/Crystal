from __future__ import absolute_import, division, print_function, unicode_literals

import os
import platform
import sys
from pathlib import Path
from zipfile import ZipFile

##### TODO IMPLEMENT SEARCHFORSERVICE

devBuild = False
launchAfter = False
forceHeart = False
forceShard = False

for arg in sys.argv:
    if arg == '-dev':
        devBuild = True
    if arg == '-launch':
        launchAfter = True
    if arg == '-forceHeart':
        forceHeart = True
    if arg == '-forceShard':
        forceShard = True
    if arg == '-help':
        print(
            "Options: -dev to install dev build; -launch to launch software after installation; -forceHeart to force the Heart to update; -forceShard to force the Shard to update;")
        sys.exit(0)

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


# Install system dependencies
print("Installing dependencies...")
if installDependencies() == 1:
    print("Dependencies require Visual C++ 14 or above. Please install those first.")
    sys.exit(1)
else:
    print("Dependencies installed.")


# TODO SEARCHFORSERVICE
def searchForService():
    return True


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


def patch(isHeart):
    print("Downloading Crystal Home Systems from repository...")
    # Download and unzip files without saving to hdd
    if devBuild:
        # Dev
        gitAddress = "https://github.com/PulsePanda/Crystal/archive/dev.zip"
    else:
        # Master
        gitAddress = "https://github.com/PulsePanda/Crystal/archive/master.zip"

    # Download the project zip file from repo
    from io import BytesIO
    from urllib.request import urlopen
    zipurl = gitAddress
    with urlopen(zipurl) as zipresp:
        with ZipFile(BytesIO(zipresp.read())) as zfile:
            # Extract the zip file without saving it to disk
            zfile.extractall(userhome + "/CrystalHomeSys/")

    dir_src = userhome + "\CrystalHomeSys\Crystal-"
    if devBuild:
        dir_src = dir_src + "dev"
    else:
        dir_src = dir_src + "master"

    print("Compiling files...")
    from subprocess import Popen

    # Build the project with Gradle
    p = Popen("gradlew assemble", shell=True, cwd=dir_src)
    stdout, stderr = p.communicate()

    # finish assigning dir_src to the proper sub directory for copy
    dir_src_nonspecific = dir_src
    if isHeart:
        dir_src = dir_src + "/Heart"
        print("Updating Heart files...")
    else:
        dir_src = dir_src + "/Shard"
        print("Updating Shard files...")

    # Unzip Heart/Shard.zip distribution file to /CrystalHomeSys/, creating the proper directories
    dir_dst = userhome + "/CrystalHomeSys/"
    if isHeart:
        zip_ref = ZipFile(dir_src + "/build/distributions/Heart.zip")
    else:
        zip_ref = ZipFile(dir_src + "/build/distributions/Shard.zip")
    zip_ref.extractall(dir_dst)
    zip_ref.close()

    print("Cleaning up...")
    import shutil
    # Remove the Crystal- project folder
    shutil.rmtree(dir_src_nonspecific)

    # If the -launch arg was passed
    if launchAfter:
        # Launch if the download is for a Heart, or if there was a force heart update
        if isHeart:
            print("Starting Heart server...")
            if "Linux" in platform.system():
                Popen("Heart", shell=True, cwd=userhome + "/CrystalHomeSys/Heart/bin/")
            elif "Windows" in platform.system():
                Popen("Heart.bat", shell=True, cwd=userhome + "/CrystalHomeSys/Heart/bin/")

        # Launch if the download is for a shard, or if there was a force shard update
        else:
            print("Starting Shard client...")
            if "Linux" in platform.system():
                Popen("Shard", shell=True, cwd=userhome + "/CrystalHomeSys/Shard/bin/")
            elif "Windows" in platform.system():
                Popen("Shard.bat", shell=True, cwd=userhome + "/CrystalHomeSys/Shard/bin/")

    if isHeart:
        print("-------------Application Launch Directory: " + dir_dst + "Heart/bin/Heart.(sh/bat)-------------")
    else:
        print("-------------Application Launch Directory: " + dir_dst + "Shard/bin/Shard.(sh/bat)-------------")


# Search for Heart DNSSD service
print("Searching for an existing Heart server...")
serviceFound = False
if not forceHeart or not forceShard:
    serviceFound = searchForService()

if serviceFound or forceShard:  # If service exists, Heart is found, returns true
    print("Found existing Heart server! Creating Shard install...")
    # Create working Shard directory
    if not os.path.isdir(userhome + "/CrystalHomeSys/Shard"):
        os.makedirs(userhome + "/CrystalHomeSys/Shard")

    print("Downloading Shard files...")
    patch(False)
elif not serviceFound or forceHeart:  # If server not found, installing heart
    print("No existing Heart server found! Creating Heart install...")
    patch(True)

sys.exit(0)
