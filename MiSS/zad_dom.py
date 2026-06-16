import numpy as np
import matplotlib.pyplot as plt
import matplotlib.animation as animation

m1,m2 = 1.0, 1.0
k1,k2,k3 = 5.0,2.0,5.0
c1,c2 = 0.1,0.1

y0 = np.array([1.5,0.0,-1.0,0.0])

t_start = 0.0
t_end = 20.0
step = 0.02
t_points = np.arange(t_start,t_end,step)

def derivatives(t,y):
    x1,v1,x2,v2 = y

    dx1_dt = v1
    dv1_dt = (-k1 * x1 + k2 * (x2-x1) - c1 * v1)/m1
    dx2_dt = v2
    dv2_dt = (-k2 * (x2-x1) -k3 * x2 - c2 *v2)/m2

    return np.array([dx1_dt,dv1_dt,dx2_dt,dv2_dt])

def rk4_step(function,t,y,step):
    k1 = function(t, y)
    k2 = function(t + 0.5 * step, y + 0.5 * step * k1)
    k3 = function(t + 0.5 * step, y + 0.5 * step * k2)
    k4 = function(t + step, y + step * k3)
    return y + (step / 6.0) * (k1 + 2*k2 + 2*k3 + k4)

y_data = np.zeros((len(t_points), 4))
y_data[0] = y0

for i in range(1, len(t_points)):
    y_data[i] = rk4_step(derivatives, t_points[i-1], y_data[i-1], step)

x1_data = y_data[:, 0]
x2_data = y_data[:, 2]

fig = plt.figure(figsize=(10, 8))
fig.canvas.manager.set_window_title('Symulacja Dynamiki - Metoda RK4')


ax_anim = plt.subplot(2, 1, 1)
ax_anim.set_xlim(-2, 12)
ax_anim.set_ylim(-1, 1)
ax_anim.set_title("Wizualizacja układu dwóch mas na sprężynach")
ax_anim.get_yaxis().set_visible(False)


eq1, eq2 = 3.0, 7.0 
mass1_box, = ax_anim.plot([], [], 's', markersize=30, color='blue', label='Masa 1')
mass2_box, = ax_anim.plot([], [], 's', markersize=30, color='red', label='Masa 2')
spring1, = ax_anim.plot([], [], color='gray', lw=2)
spring2, = ax_anim.plot([], [], color='gray', lw=2)
spring3, = ax_anim.plot([], [], color='gray', lw=2)
ax_anim.legend(loc="upper right")


ax_plot = plt.subplot(2, 1, 2)
ax_plot.plot(t_points, x1_data, label='Wychylenie x1', color='blue')
ax_plot.plot(t_points, x2_data, label='Wychylenie x2', color='red')
ax_plot.set_title("Wykres położenia mas w czasie")
ax_plot.set_xlabel("Czas [s]")
ax_plot.set_ylabel("Wychylenie [m]")
ax_plot.grid(True)
ax_plot.legend()
plt.tight_layout()


def update(frame):
    pos1 = eq1 + x1_data[frame]
    pos2 = eq2 + x2_data[frame]
    
    mass1_box.set_data([pos1], [0])
    mass2_box.set_data([pos2], [0])
    
    spring1.set_data([0, pos1], [0, 0])
    spring2.set_data([pos1, pos2], [0, 0])
    spring3.set_data([pos2, 10], [0, 0])
    
    return mass1_box, mass2_box, spring1, spring2, spring3

ani = animation.FuncAnimation(fig, update, frames=len(t_points), interval=step*1000, blit=True)

plt.show()