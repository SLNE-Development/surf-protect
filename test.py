import matplotlib.pyplot as plt
import numpy as np
from matplotlib.widgets import TextBox

money_per_block = 4
spawn_protection = 100
max_protection = 10000
break_point = 1000

# Values
xpoints = np.array(range(0, max_protection))


def yfunc(x_array, multiplier=5):
    y_array = []

    for x in x_array:
        if x < spawn_protection:
            y_array.append(0)
        else:
            if x > break_point:
                y_array.append(money_per_block)
            else:
                y_array.append(
                    money_per_block * multiplier - (x / 1000) * money_per_block
                )

    return y_array


# Apply function to all values in xpoints
ypoints = yfunc(xpoints)


# Plot all values
plt.plot(
    xpoints,
    ypoints,
    "-o",
    color="red",
    linewidth=1,
    markerfacecolor="black",
    markeredgecolor="black",
    markersize=2,
)

# Show plot
plt.show()
