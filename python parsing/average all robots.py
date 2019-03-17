import os

allaverages = []
REPLACE = [
    [";", ":"],
    ["true", "1"],
    ["false", "0"],
    ["right", "1"],
    ["center", "0"],
    ["left", "-1"],
    ["Did not return", "0"],
    ["Returned to hab (Level 1)", "1"],
    ["Level 1 (HAB)", "1"],
    ["Level 1", "1"],
    ["Level 2", "2"],
    ["Level 3", "3"]
]
for flag in REPLACE:
    flag[0] = flag[0].lower()


def getdataforfile(filename, index, robotnumber):
    if filename.endswith(".csv"):  # If it's a .csv:
        lines = tuple(open(filename, 'r'))
        lines = list(lines)
        print(filename)
        global allaverages
        data = []
        averages = []

        dates = []
        for line in lines:

            if(lines.index(line) != 0):
                for flag in REPLACE:
                    if flag[0] in line:
                        print(flag)
                        while flag[0] in line:
                            line = line.lower().replace(flag[0], flag[1])

                if line.split(",")[0] in dates:
                    continue
                dates.append(line.split(",")[0])
                linedata = line.split(",")
                othervariable = []
                for linething in linedata[:]:
                    if linedata.index(linething) == 72:
                        othervariable.append(linething)
                        linedata.remove(linething)
                linedata.append('{}. By: {} , '.format(othervariable[0].replace("||", "|").replace("|n", "  ").replace("|q", '"').replace(";", ":").replace("|ob", "{").replace("|cb", "}"), othervariable[1]))
                data.append(linedata)
        print(len(data))
        for i in range(len(data[0])):
            totalsum = 0
            fullString = ""  # used for comments
            for roundnum in data:  # actual data
                try:
                    roundnum = float(roundnum[i])
                    totalsum += roundnum
                except ValueError:
                    fullString += roundnum[i]
            if i == 0:
                averages.append(len(data))
            elif i == 72:
                averages.append(fullString[:-3])
            else:
                averages.append(totalsum/(len(data)))

        allaverages.append(robotnumber.replace(".csv", "")+",")
        for average in averages:
            allaverages[index] += str(average) + ","


foldername = input("folderpath: ")
if not foldername.endswith("\\"):
    foldername += "\\"

if not os.path.exists(foldername + '\\results\\'):
    os.makedirs(foldername + '\\results\\')

files = os.listdir(foldername)
print(files)
files.remove("results")
files.remove("EventData")

# files = ["772.csv"]

for i in range(len(files)):
    getdataforfile(foldername + files[i], i, files[i])

lines = tuple(open(foldername+files[0], 'r'))


f = open(foldername + '\\results\\all robots averaged.csv', 'w')
f.write("Robot Number," + lines[0])

for lineofaverages in allaverages:
    f.write("\n" + lineofaverages)

print(allaverages)

f.close()
