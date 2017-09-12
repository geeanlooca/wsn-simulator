import os

framesize = [250, 600, 950, 1300, 1650, 2000, 2346]
time=2400
protocol = "GALTIER"
nodes = [5, 20, 50]
scheme = "paper"
runs = 10

for n in nodes:
    file = "./{0}-{1}-framesize-{2}nodes.csv".format(protocol, scheme, str(n))

    if (os.path.isfile(file)):
        os.remove(file)

    for f in framesize:
        print  "\n\nTESTING WITH {0} NODES AND {1} BYTES".format(str(n), str(f))
        cmd = "java -Dscheme=\"{0}\" -Dframesize={1} -Dnodes=\"{2}\" -Dtime={3} -Doutput={4} -Dprotocol=\"{5}\" -jar wsn-simulator.jar ".format(
        scheme, str(f), str(n), str(time), file, protocol)
        os.system(cmd)