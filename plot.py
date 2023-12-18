import matplotlib.pyplot as plt
import numpy as np

def calculate_mean_time(file_path):
    print(f"Calculating mean time for {file_path}")
    try:
        with open(file_path) as f:
            measurements = f.readlines()
        measurements = [int(x.strip()) for x in measurements[1:]]
        print(measurements)
        return sum(measurements) / len(measurements)
    except FileNotFoundError:
        print(f"File {file_path} not found.")
        return 0
mean_pip_time = []
mean_pdp_time = []
files = [4, 8, 16, 32, 64]

for file in files:
    extraction_file_path = f'measurements/{file}/pip-{file}.txt'
    mean_pip_time.append(calculate_mean_time(extraction_file_path))

    pdp_file_path = f'measurements/{file}/pdp-{file}.txt'
    mean_pdp_time.append(calculate_mean_time(pdp_file_path))

print(mean_pip_time)
print(mean_pdp_time)


labels = ['4', '8', '16', '32', '64']
x = np.arange(len(labels))
plt.bar(x, mean_pip_time, label='PIP')
plt.bar(x, mean_pdp_time, bottom=mean_pip_time, label='PDP')
plt.xlabel('Number of attributes per policy')
plt.ylabel('Mean time (ms)')
plt.title('execution time (multiple DBs)')
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

    labels = ['PDP', 'PIP']
    sizes = [ mean_pdp_time, mean_pip_time]
    percentages = [s/sum(sizes)*100 for s in sizes]
    legend_labels = ['{0} - {1:1.1f} %'.format(i,j) for i,j in zip(labels, percentages)]
    plt.pie(sizes, startangle=90)
    plt.legend( legend_labels, bbox_to_anchor=(-0.40, 1),loc='upper left')
    plt.title(f'Execution time of {file}-attributes policy (multiple DBs)')
    plt.savefig(f'figures/pie-{file}.png')
    plt.clf()
