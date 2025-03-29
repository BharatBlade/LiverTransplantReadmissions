import pandas as pd
import sys, getopt
data = pd.io.stata.read_stata(sys.argv[1])
data.to_csv(sys.argv[2])
