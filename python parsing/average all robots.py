import os

allaverages = []

def getdataforfile(filename, index, robotnumber):
    lines = tuple(open(filename, 'r'))
    lines = list(lines)
    print(filename)
    global allaverages
    data = []
    averages = []

    dates = []
    for line in lines:
        if(lines.index(line) != 0):
            if line.split(",")[0] in dates:
                continue
            dates.append(line.split(",")[0])
            linedata = line.split(",")
            othervariable = ""
            for linething in linedata[:]:
                if linedata.index(linething) >= 17:
                    othervariable += linething
                    linedata.remove(linething)
            linedata.append(othervariable)
            data.append(linedata)

            
    print(len(data))
    for i in range(len(data[0])):
        totalsum = 0
        pegPlacedAmount = 0
        fullString = "" #used for comments
        for roundnum in data: #actual data
            if i== 17:
                try:
                    fullString += str(roundnum[i]) + "\t\t:\t\t"
                except IndexError as verr:
                    continue
            try:
                roundnum = int(roundnum[i])
            except ValueError as verr:
                continue
            except IndexError as verr:
                continue
            if i==16:
                if roundnum == -1:
                    pegPlacedAmount += 1
            else:
                totalsum += roundnum
        if i == 1:
            averages.append(len(data))
        elif i == 16:
            averages.append(str((len(data)-pegPlacedAmount)/(len(data))))
        elif i == 23:
            
            averages.append('"' + fullString.replace('"',"").replace(",",".") + '"')
        else:
            averages.append(totalsum/(len(data)))
    
    allaverages.append(robotnumber+",")
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

#files = ["772.csv"]

for i in range(len(files)):
    getdataforfile(foldername + files[i], i, files[i])

lines = tuple(open(foldername+files[0], 'r'))


f = open(foldername + '\\results\\all robots.csv', 'w')
f.write("Robot Number," + lines[0])

for lineofaverages in allaverages:
    f.write("\n" + lineofaverages)

print(allaverages)

f.close()  
