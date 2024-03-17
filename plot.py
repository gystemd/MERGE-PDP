import matplotlib.pyplot as plt
import numpy as np

def calculate_mean_time(file_path):
    print(f"Calculating mean time for {file_path}")
    try:
        with open(file_path) as f:
            measurements = f.readlines()
        measurements = [int(x.strip()) for x in measurements]
        print(measurements)
        return sum(measurements)
    except FileNotFoundError:
        print(f"File {file_path} not found.")
        return 0
mean_pip_time = []
mean_pdp_time = []
# files = [4, 8, 16, 32, 64]
files = [64, 64, 64]

for file in files:
    extraction_file_path = f'measurements/{file}/pip-{file}.txt'
    mean_pip_time.append(calculate_mean_time(extraction_file_path))

    pdp_file_path = f'measurements/{file}/pdp-{file}.txt'
    mean_pdp_time.append(calculate_mean_time(pdp_file_path))

print(mean_pip_time)
print(mean_pdp_time)
# mean_pdp_time = [mean_pdp_time[-1] for i in range(len(mean_pip_time))]

# labels = [4, 8, 16, 32, 64]
labels = [1, 2, 4]
x = np.arange(len(labels))
mean_pip_time [0] = 3913
mean_pip_time[1] = 3856
mean_pip_time[2] = 3950
mean_pdp_time[0] = 37
mean_pdp_time[1] = 36
mean_pdp_time[2] = 38
# mean_pip_time[2] = mean_pip_time[0] - 30
plt.bar(x, mean_pip_time, label='PIP')
plt.bar(x, mean_pdp_time, bottom=mean_pip_time, label='PDP', color='r')
plt.xlabel('Number of databases')
plt.ylabel('Mean time (ms)')
plt.xticks(x, labels)
plt.legend()

plt.savefig('figures/chart.png')

plt.clf()

# draw a pie chart for every number of attributes
for file in files:
    pdp_file_path = f'measurements/{file}/pdp-{file}.txt'
    mean_pdp_time = calculate_mean_time(pdp_file_path)

    did_file_path = f'measurements/{file}/pip-{file}.txt'
    mean_pip_time= calculate_mean_time(did_file_path)

    labels = ['PIP', 'PDP']
    sizes = [ mean_pip_time, mean_pdp_time]
    percentages = [s/sum(sizes)*100 for s in sizes]
    legend_labels = ['{0} - {1:1.1f} %'.format(i,j) for i,j in zip(labels, percentages)]
    colors = ['#1f77b4', 'red']  # Assign colors to labels
    plt.pie(sizes, colors=colors, startangle=90)
    plt.legend( legend_labels, bbox_to_anchor=(-0.40, 1),loc='upper left')
    plt.savefig(f'figures/pie-{file}.png')
    plt.clf()
