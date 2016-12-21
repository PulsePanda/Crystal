import zipfile
import sys

dir = sys.argv[1]
out = sys.argv[2]

zip_ref = zipfile.ZipFile(dir, 'r')
zip_ref.extractall(out)
zip_ref.close()
