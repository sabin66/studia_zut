import matplotlib.pyplot as plt
import random
import math

N = 100
T = 100
MAP_SIZE = 100
MAX_AGE = 100
MAX_POP = 500
STATES = ['C', 'Z', 'ZD', 'ZZ']
COLORS = {'C': (1, 0, 0), 'Z': (1, 1, 0), 'ZD': (1, 0.5, 0), 'ZZ': (0, 1, 0)}

class Person:
    def __init__(self):
        self.x = 0.0
        self.y = 0.0
        self.vx = 0.0
        self.vy = 0.0
        self.state = 'ZZ'
        self.age = 0
        self.immunity = 0.0
        self.days_in_state = 0
        self.alive = True

def init_immunity(age):
    if age < 15 or age > 70:
        return random.random() * 3
    elif age < 40:
        return 6 + random.random() * 4
    else:
        return 3 + random.random() * 3

def max_age_immunity(age):
    if age < 15 or age > 70:
        return 3
    elif age < 40:
        return 10
    else:
        return 6

def interact(a, b):
    if a.state == 'ZZ' and b.state == 'Z':
        if a.immunity < 3:
            a.state = 'Z'
            a.days_in_state = 0
    elif a.state == 'ZZ' and b.state == 'C':
        if a.immunity < 6:
            a.state = 'Z'
            a.days_in_state = 0
        else:
            a.immunity = max(0, a.immunity - 3)
    elif a.state == 'Z' and b.state == 'Z':
        a.immunity = max(0, a.immunity - 1)
        b.immunity = max(0, b.immunity - 1)
    elif a.state == 'ZD' and b.state == 'Z':
        a.immunity = max(0, a.immunity - 1)
    elif a.state == 'ZZ' and b.state == 'ZZ':
        a.immunity = max(a.immunity, b.immunity)
        b.immunity = a.immunity

def newborn(parent):
    speed = random.randint(1, 3)
    angle = 2 * math.pi * random.random()
    baby = Person()
    baby.x = parent.x + random.gauss(0, 1) * 2
    baby.y = parent.y + random.gauss(0, 1) * 2
    baby.vx = speed * math.cos(angle)
    baby.vy = speed * math.sin(angle)
    baby.state = 'ZZ'
    baby.age = 0
    baby.immunity = max_age_immunity(0)
    baby.days_in_state = 0
    baby.alive = True
    return baby

population = []
for _ in range(N):
    p = Person()
    speed = random.randint(1, 3)
    angle = 2 * math.pi * random.random()
    age = random.randint(0, 60)
    
    p.x = random.randint(1, MAP_SIZE)
    p.y = random.randint(1, MAP_SIZE)
    p.vx = speed * math.cos(angle)
    p.vy = speed * math.sin(angle)
    p.state = random.choice(STATES)
    p.age = age
    p.immunity = init_immunity(age)
    p.days_in_state = 0
    p.alive = True
    
    population.append(p)

plt.ion()
fig, ax = plt.subplots(figsize=(7, 7))

for t in range(1, T + 1):
    ax.clear()
    ax.set_xlim(0, MAP_SIZE)
    ax.set_ylim(0, MAP_SIZE)
    ax.set_title(f'Tura {t}')
    
    for p in population:
        if p.alive:
            ax.plot(p.x, p.y, 'o', color=COLORS[p.state], markersize=7)
    
    plt.pause(0.05)
    
    for p in population:
        if not p.alive:
            continue
            
        p.age += 1
        if p.age > MAX_AGE or p.immunity <= 0:
            p.alive = False
            continue
            
        max_imm = max_age_immunity(p.age)
        if p.immunity > max_imm:
            p.immunity = max_imm
            
        p.days_in_state += 1
        
        if p.state == 'Z':
            if p.days_in_state >= 2:
                p.state = 'C'
                p.days_in_state = 0
            p.immunity = max(0, p.immunity - 0.1)
        elif p.state == 'C':
            if p.days_in_state >= 7:
                p.state = 'ZD'
                p.days_in_state = 0
            p.immunity = max(0, p.immunity - 0.5)
        elif p.state == 'ZD':
            if p.days_in_state >= 5:
                p.state = 'ZZ'
                p.days_in_state = 0
            p.immunity = min(max_imm, p.immunity + 0.1)
        elif p.state == 'ZZ':
            p.immunity = min(max_imm, p.immunity + 0.05)
            
        p.x += p.vx
        p.y += p.vy
        
        if p.x < 0 or p.x > MAP_SIZE:
            p.vx = -p.vx
        if p.y < 0 or p.y > MAP_SIZE:
            p.vy = -p.vy

    pop_size = len(population)
    new_kids = []
    
    for i in range(pop_size):
        pi = population[i]
        if not pi.alive:
            continue
        
        for j in range(i + 1, pop_size):
            pj = population[j]
            if not pj.alive:
                continue
                
            dx = abs(pi.x - pj.x)
            dy = abs(pi.y - pj.y)
            
            if max(dx, dy) <= 2:
                pi.vx, pj.vx = pj.vx, pi.vx
                pi.vy, pj.vy = pj.vy, pi.vy
                
                interact(pi, pj)
                
                if (20 <= pi.age <= 40) and (20 <= pj.age <= 40) and (random.random() < 0.25):
                    kids_count = random.randint(1, 2)
                    for _ in range(kids_count):
                        if len(population) + len(new_kids) < MAX_POP:
                            new_kids.append(newborn(pi))

    population.extend(new_kids)

plt.ioff()
plt.show()