import pandas as pd
import csv
from statistics import mean
import matplotlib.pyplot as plt
import seaborn as sns


total_values_to_be_taken = 200

writer = csv.writer(open("output.csv", "w"), quoting=csv.QUOTE_NONE)
reader = csv.reader(open("1.csv", "r"), skipinitialspace=True)
writer.writerows(reader)

input_data = pd.read_csv("output.csv", sep=";", engine='python')
column1 = input_data[input_data.columns[1]]
column2 = input_data[input_data.columns[2]]
column3 = input_data[input_data.columns[3]]
pitch = input_data[input_data.columns[12]]

signal_magnitude_vector =[]
for rows in range(0, total_values_to_be_taken):
    signal_magnitude_vector.append(((column1[rows] ** 2) + (column2[rows] ** 2) + (column3[rows] ** 2)) ** (1 / 2))

last_columns_of_result = signal_magnitude_vector[-50:]
First50thdata_sample = signal_magnitude_vector[50]
last15_columns_of_result = pitch[-15:]
average_of_last_columns = mean(last_columns_of_result)

standard_deviation = []
for results in last_columns_of_result:
    standard_deviation.append((results - average_of_last_columns) ** 2)

detailcoeff_X = []
detailcoeff_Y = []
detailcoeff_Z = []
detailcoeff_SMV = []
for rows in range(0, 90):
    detailcoeff_X.append((1/1.414) * column1[2*rows] - column1[2*rows+1]);
    detailcoeff_Y.append((1 / 1.414) * column2[2 * rows] - column2[2 * rows + 1]);
    detailcoeff_Z.append((1 / 1.414) * column3[2 * rows] - column3[2 * rows + 1]);

for rows in range(0, 90):
    detailcoeff_SMV.append(((detailcoeff_X[rows] ** 2) + (detailcoeff_Y[rows] ** 2) + (detailcoeff_Z[rows] ** 2)) ** (1 / 2))

result = pd.DataFrame({'value': signal_magnitude_vector})
print("total results", result)
print("50th data sample of SMV", First50thdata_sample)
print("SMV result", result.mean())
print("standard Deviation", (0.02 * sum(standard_deviation) * .5))
print("Orientation result", (sum(last15_columns_of_result)/15))

plt.title("Sequence of ax[n]")
plt.xlabel('Data Point')
plt.ylabel('Gravitational Acceleration(G)')
plt.plot(column1)
plt.show()

plt.title("Sequence of ay[n]")
plt.xlabel('Data Point')
plt.ylabel('Gravitational Acceleration(G)')
sns.kdeplot(result["value"], shade=True)
plt.plot(column2)
plt.show()

plt.title("Sequence of az[n]")
plt.xlabel('Data Point')
plt.ylabel('Gravitational Acceleration(G)')
sns.kdeplot(result["value"], shade=True)
plt.plot(column3)
plt.show()

plt.title("Sequence of S[n] ")
plt.xlabel('Data Point')
plt.ylabel('Gravitational Acceleration(G)')
sns.kdeplot(result["value"], shade=True)
plt.plot(result)
plt.show()

plt.title("Sequence of Pitch angle")
plt.xlabel('Data Point')
plt.ylabel('Angle')
sns.kdeplot(result["value"], shade=True)
plt.plot(pitch)
plt.show()

plt.title("Detail coefﬁcients of X -  Haar wavelet transform")
plt.xlabel('Data Point')
plt.ylabel('Gravitational Acceleration(G)')
sns.kdeplot(result["value"], shade=True)
plt.plot(detailcoeff_X)
plt.show()

plt.title("Detail coefﬁcients of Y -  Haar wavelet transform")
plt.xlabel('Data Point')
plt.ylabel('Gravitational Acceleration(G)')
sns.kdeplot(result["value"], shade=True)
plt.plot(detailcoeff_Y)
plt.show()

plt.title("Detail coefﬁcients of Z -  Haar wavelet transform")
plt.xlabel('Data Point')
plt.ylabel('Gravitational Acceleration(G)')
sns.kdeplot(result["value"], shade=True)
plt.plot(detailcoeff_Z)
plt.show()

plt.title("Detail coefﬁcients of S[n] -  Haar wavelet transform")
plt.xlabel('Data Point')
plt.ylabel('Gravitational Acceleration(G)')
sns.kdeplot(result["value"], shade=True)
plt.plot(detailcoeff_SMV)
plt.show()




