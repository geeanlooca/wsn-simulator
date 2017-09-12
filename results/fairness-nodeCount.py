import os
import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
import itertools


# plt.rc('text', usetex=True)
# plt.rc('font', family='serif')


framesize = 1500
protocol = "DCF"
nodes = [5, 10, 20, 50]
file = "./{0}-paper-fairness-nodes-{1}-framesize.csv".format(protocol, framesize)

if (os.path.isfile(file)):
    os.remove(file)

for j in range(10):
    for i in nodes:
        cmd = "/Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/bin/java -Dscheme=\"paper\" -Dframesize={0} -Dnodes=\"{1}\" -Dtime=2400 -Doutput={2} -Dprotocol=\"{3}\" -jar wsn-simulator-master\ 2.jar ".format(
            str(framesize), str(i), file, protocol)
        os.system(cmd)


#
# df = pd.read_csv(file, sep=';')
# nodecounts = df['nodecount'].unique()
#
# fig = plt.figure()
# for i,ms in zip(nodecounts, itertools.cycle('s^+*>')):
#     subdf = df[df['nodecount'] == i]
#     fsize = subdf['framesize']
#     delay = subdf['delay']
#     throughput = subdf['throughput']
#     plt.plot(fsize, delay, label="n = " + str(i), marker=ms)
#
# plt.xlabel("Framesize [bytes]")
# plt.ylabel("Delay [ms]")
# plt.xticks(fsize)
# plt.legend()
# plt.show()

# fig = plt.figure()
# for i,ms in zip(nodecounts, itertools.cycle('s^+*>')):
#     subdf = df[df['nodecount'] == i]
#     fsize = subdf['framesize']
#     delay = subdf['delay']
#     throughput = subdf['throughput']
#     plt.plot(fsize, throughput, label="n = " + str(i), marker=ms)
#
# plt.xlabel("Framesize [bytes]")
# plt.ylabel("Normalized throughput")
# plt.xticks(fsize)
# plt.legend()
# plt.show()