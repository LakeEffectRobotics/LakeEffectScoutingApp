import os

class Score:
    def __init__(self):
        self.success = 0;
        self.fail = 0;
    def __str__(self):
        return str(self.success)+","+str(self.fail)  
        
    def score(self):
        self.success+=1
    
    def miss(self):
        self.fail+=1;
        
    def multiScore(self, add):
        self.success+=add
        
    def multiMiss(self, add):
        self.fail+=add
    
    def getAccuracy(self):
        if(self.success+self.fail==0):
            return 0;
        if(self.fail==0):
            return 1;
        return(abs(self.success/float(self.getTotal())))

    def getTotal(self):
        return(self.success+self.fail)
        
class Robot:
    def __init__(self,number):
        self.number = number;
        self.matches = 0;
        self.baseline = 0;
        self.firstAutoSwitch = Score();
        self.firstAutoScale = Score();
        self.firstAutoVault = Score();
        self.firstAutoNone = 0;
        self.secondAutoSwitch = Score();
        self.secondAutoScale = Score();
        self.secondAutoVault = Score();
        self.secondAutoNone = 0;
        self.autoCubePickedup = 0;
        self.ownSwitch = Score();
        self.scale = Score();
        self.otherSwitch = Score();
        self.vault = Score();
        self.climb = Score();
        self.climbAssisted = 0;
        self.climbCarried = 0;
        self.defense = 0;
        self.died = 0;
        self.tipped = 0;
        self.driveRating = 0;
        self.intakeRating = 0;
        self.comments = "";
        self.scouts = "";
    
    def __str__(self):
        return str(self.matches) + ":" + str(self.baseline) +":"+\
            str(self.firstAutoNone)+":"+str(self.firstAutoScale)+":"+str(self.firstAutoSwitch)+":"+str(self.firstAutoVault)+":"+\
            str(self.autoCubePickedup)+":"+str(self.climb)+":"+str(self.defense)+":"+str(self.died)+":"+str(self.tipped)+":"+\
            str(self.driveRating)+":"+self.comments+":"+self.scouts;
    
    
    def addMatch(self, match):
        match = match.replace(";",":").replace("False","0").replace("True","1").replace("false","0").replace("true","1");
        print(match);
        data = match.split(",");
        self.matches+=1;
        self.baseline+=int(data[2]);
        
        location = data[3];
        #First Auto Cube
        if(location == "None"):
                self.firstAutoNone +=1;
        elif(data[4] == "1"):
            if(location == "Scale"):
                self.firstAutoScale.score();
                print("Auto Scale scored")
            if(location == "Switch"):
                self.firstAutoSwitch.score();
            if(location == "Vault"):
                self.firstAutoVault.score();
        else:
            print("First miss")
            if(location == "Scale"):
                self.firstAutoScale.miss();
            if(location == "Switch"):
                self.firstAutoSwitch.miss();
            if(location == "Vault"):
                self.firstAutoVault.miss();
                
        location = data[5];
        #Second Auto Cube
        print(location+"\t"+data[6])
        if(location == "None"):
                self.secondAutoNone +=1;
        elif(data[6] == "1"):
            if(location == "Scale"):
                self.secondAutoScale.score();
            if(location == "Switch"):
                self.secondAutoSwitch.score();
            if(location == "Vault"):
                self.secondAutoVault.score();
        else:
            if(location == "Scale"):
                self.secondAutoScale.miss();
            if(location == "Switch"):
                self.secondAutoSwitch.miss();
            if(location == "Vault"):
                self.secondAutoVault.miss();
                
        print(data[4])
        print(self.firstAutoScale)
        print(self.secondAutoSwitch)
        
        
        self.autoCubePickedup+=int(data[7])
        
        self.ownSwitch.multiScore(int(data[8]))
        self.ownSwitch.multiMiss(int(data[9]))
        self.scale.multiScore(int(data[10]))
        self.scale.multiMiss(int(data[11]))
        self.otherSwitch.multiScore(int(data[12]))
        self.otherSwitch.multiMiss(int(data[13]))
        self.vault.multiScore(int(data[14]))
        self.vault.multiMiss(int(data[15]))
        
        
        if(data[16] == "Successful Climb"):
            self.climb.score()
        elif(data[17] == "Failed Climb"):
            self.climb.miss()
        
        self.defense+=int(data[18])
        self.died+=int(data[19])
        self.tipped+=int(data[20])
        
        self.driveRating+=float(data[21])
        self.intakeRating+=float(data[22])
        
        self.comments+=data[23]+":"
        
        self.scouts += data[24]+":";
        
    def averageAll(self):
        averages = []
        labels = []
        labels.append("Robot")
        averages.append(self.number)
        labels.append("Match Count")
        averages.append(self.matches);
        labels,averages = self.auto(labels,averages);
        labels,averages = self.tele(labels,averages);
        labels.append("Comments")
        averages.append(self.comments)
        return(join(labels),join(averages))
    
    def averageAuto(self):
        averages = []
        labels = []
        labels.append("Robot")
        averages.append(self.number)
        labels.append("Match Count")
        averages.append(self.matches);
        labels,averages = self.auto(labels,averages);
        labels.append("Comments")
        averages.append(self.comments)
        return(join(labels),join(averages))
    
    def averageTele(self):
        averages = []
        labels = []
        labels.append("Robot")
        averages.append(self.number)
        labels.append("Match Count")
        averages.append(self.matches);
        labels,averages = self.tele(labels,averages);
        labels.append("Comments")
        averages.append(self.comments)
        return(join(labels),join(averages))
        
    def auto(self,labels,averages):
        labels.append("Auto Baseline")
        averages.append(self.baseline/self.matches)
        
        labels.append("First Auto Attempt")
        averages.append((self.matches-self.firstAutoNone)/self.matches)
        
        labels.append("First Auto Switch Attempts")
        averages.append(self.firstAutoSwitch.getTotal())
        labels.append("First Auto Switch Success")
        averages.append(self.firstAutoSwitch.success)
        labels.append("First Auto Switch Accuracy")
        averages.append(self.firstAutoSwitch.getAccuracy())
        
        labels.append("First Auto Scale")
        averages.append(self.firstAutoScale.getTotal())
        labels.append("First Auto Scale Success")
        averages.append(self.firstAutoScale.success)
        labels.append("First Auto Scale Accuracy")
        averages.append(self.firstAutoScale.getAccuracy())
        
        labels.append("First Auto Vault")
        averages.append(self.firstAutoVault.getTotal())
        labels.append("First Auto Vault Success")
        averages.append(self.firstAutoVault.success)
        labels.append("First Auto Vault Accuracy")
        averages.append(self.firstAutoVault.getAccuracy())
        
        labels.append("Second Auto Attempt")
        averages.append((self.matches-self.secondAutoNone)/self.matches)
       
        labels.append("Second Auto Switch")
        averages.append(self.secondAutoSwitch.getTotal())
        labels.append("Second Auto Switch Success")
        averages.append(self.secondAutoSwitch.success)
        labels.append("Second Auto Switch Accuracy")
        averages.append(self.secondAutoSwitch.getAccuracy())
        
        labels.append("Second Auto Scale")
        averages.append(self.secondAutoScale.getTotal())
        labels.append("Second Auto Scale Success")
        averages.append(self.secondAutoScale.success)
        labels.append("Second Auto Scale Accuracy")
        averages.append(self.secondAutoScale.getAccuracy())
        
        labels.append("Second Auto Vault")
        averages.append(self.secondAutoVault.getTotal())
        labels.append("Second Auto Vault Success")
        averages.append(self.secondAutoVault.success)
        labels.append("Second Auto Vault Accuracy")
        averages.append(self.secondAutoVault.getAccuracy())
        return [labels,averages]
        
    def tele(self,labels,averages):
        labels.append("Own Switch Attempts")
        averages.append(self.ownSwitch.getTotal())
        labels.append("Own Switch Success")
        averages.append(self.ownSwitch.success/self.matches)
        labels.append("Own Switch Accuracy")
        averages.append(self.ownSwitch.getAccuracy())
        
        labels.append("Scale Attempts")
        averages.append(self.scale.getTotal())
        labels.append("Scale Success")
        averages.append(self.scale.success/self.matches)
        labels.append("Scale Accuracy")
        averages.append(self.scale.getAccuracy())
        
        labels.append("Other Switch Attempts")
        averages.append(self.otherSwitch.getTotal())
        labels.append("Other Switch Success")
        averages.append(self.otherSwitch.success/self.matches)
        labels.append("Other Switch Accuracy")
        averages.append(self.otherSwitch.getAccuracy())
        
        labels.append("Vault Attempts")
        averages.append(self.vault.getTotal())
        labels.append("Vault Success")
        averages.append(self.vault.success/self.matches)
        labels.append("Vault Accuracy")
        averages.append(self.vault.getAccuracy())
        
        labels.append("Climb Average")
        averages.append(self.climb.getAccuracy())
        
        labels.append("Defense")
        averages.append(self.defense/self.matches)
        labels.append("Died")
        averages.append(self.died/self.matches)
        labels.append("Tipped")
        averages.append(self.tipped/self.matches)
        labels.append("Drive Rating")
        averages.append(self.driveRating/self.matches)
        return [labels,averages]
    
    
def join(data):
    out = ""
    for i in data:
        out+=str(i)+","
    return(out)
    
foldername = input("Folder path: ")
if not foldername.endswith("\\"):
    foldername += "\\"

if not os.path.exists(foldername + 'results\\'):
    os.makedirs(foldername + 'results\\')

files = os.listdir(foldername)
files.remove("results")
files.remove("EventData")
print(files)

robots = []

for file in files:
    lines = list(open(foldername + file,'r'))
    print(file.replace(".csv",""))
    print(lines)
    
    bot = Robot(file.replace(".csv",""))
    lines.pop(0)
    for line in lines:
        bot.addMatch(line)
    print(bot)
    robots.append(bot)

# Create CSV Files
allAverages = open(foldername+"results\\allAverages.csv",'w')
autoAverages = open(foldername+"results\\autoAverages.csv",'w')
teleAverages = open(foldername+"results\\teleAverages.csv",'w')

# Add Labels
allAverages.write(robots[0].averageAll()[0]+"\n")
autoAverages.write(robots[0].averageAuto()[0]+"\n")
teleAverages.write(robots[0].averageTele()[0]+"\n")    

# Add Data
for robot in robots:
    allAverages.write(robot.averageAll()[1]+"\n")
    autoAverages.write(robot.averageAuto()[1]+"\n")
    teleAverages.write(robot.averageTele()[1]+"\n")    
    
# Close files
allAverages.close()
autoAverages.close()
teleAverages.close()