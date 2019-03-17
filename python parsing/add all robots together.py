import os
foldername = input("folderpath: ")
if not foldername.endswith("\\"):
    foldername += "\\"

alllines = []


def getdataforfile(filename, index, robotnumber):
    if filename.endswith(".csv"):  # Don't open directories
        robotnumber = robotnumber.replace(".csv", "")
        lines = tuple(open(filename, 'r'))
        lines = list(lines)
        lines.pop(0)
        for line in lines:
            line = line.replace(";", " ")
            line = robotnumber + "," + line
            alllines.append(line)


if not os.path.exists(foldername + '\\results\\'):
    os.makedirs(foldername + '\\results\\')

files = os.listdir(foldername)

files.remove("results")
files.remove("EventData")
files.remove("AutoEventData")

for i in range(len(files)):
    getdataforfile(foldername + files[i], i, files[i])

lines = tuple(open(foldername+files[0], 'r'))


f = open(foldername + '\\results\\all robots.csv', 'w')
f.write("Robot Number," + lines[0])

for line in alllines:
    f.write(line)

f.close()
