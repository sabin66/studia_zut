import random
import csv
import statistics
import os

DEF_N = 10
DEF_POPULATION_SIZE = 50
DEF_GEN_MAX = 1000
DEF_CROSSOVER_PROB = 0.7
DEF_MUTATION_PROB = 0.05
DEF_TOURNAMENT_SIZE = 5
LOG_INTERVAL = 5

class EA:
    def __init__(self,n,population_size,crossover_prob,mutation_prob,tournament_size):
        self.n = n
        self.population_size = population_size
        self.crossover_prob = crossover_prob
        self.mutation_prob = mutation_prob
        self.tournament_size = tournament_size

    def init_population(self):
        return [[random.randint(0,self.n-1) for _ in range(self.n)] for _ in range(self.population_size)]
    
    def fitness_function(self,individual):
        conflicts = 0
        n = len(individual)

        for i in range(n):
            for j in range(i+1,n):
                if individual[i] == individual[j]:
                    conflicts+=1
                elif abs(i-j) == abs(individual[i]-individual[j]):
                    conflicts+=1

        return conflicts
    
    def selection(self,population,fitness):
        selected = []
        for _ in range(self.population_size):
            fighters = random.sample(range(self.population_size), self.tournament_size)
            best_index = fighters[0]
            best_fit = fitness[best_index]

            for index in fighters[1:]:
                if fitness[index] < best_fit:
                    best_fit = index
                    best_fit = fitness[index]

            selected.append(list(population[best_index]))

        return selected
    
    def crossover(self,population):
        for i in range(0,self.population_size-1,2):
            if random.random() < self.crossover_prob:
                cut = random.randint(1,self.n-1)
                parent1 = population[i]
                parent2 = population[i+1]

                population[i] = parent1[:cut] + parent2[cut:]
                population[i+1] = parent1[cut:] + parent2[:cut]

        return population
    
    def mutation(self,population):
        for i in range(self.population_size):
            for gene_index in range(self.n):
                if random.random() < self.mutation_prob:
                    current_val = population[i][gene_index]
                    possible_vals = list(range(self.n))
                    possible_vals.remove(current_val)

                    if possible_vals:
                        population[i][gene_index] = random.choice(possible_vals)

        return population
    
    def run(self,max_generations,run_id,parameter_label,csv_writer,log_interval=LOG_INTERVAL):
        population = self.init_population()
        fitness = [self.fitness_function(individual) for individual in population]
        best_overall_fit = float('inf')
        best_overall_individual = None
        for gen in range(max_generations+1):
            current_best_fit = min(fitness)
            mean_fit = statistics.mean(fitness)

            if current_best_fit < best_overall_fit:
                best_overall_fit = current_best_fit
                best_index = fitness.index(current_best_fit)
                best_overall_individual = list(population[best_index])

            if gen % log_interval == 0 or gen == max_generations:
                 csv_writer.writerow([gen, run_id, parameter_label, f"{mean_fit:.2f}", current_best_fit])

            if best_overall_fit == 0:
                if gen % log_interval != 0:
                    csv_writer.writerow([gen, run_id, parameter_label, f"{mean_fit:.2f}", best_overall_fit])
                break

            offspring = self.selection(population,fitness)

            offspring = self.crossover(offspring)

            offspring = self.mutation(offspring)

            offspring_fitnesses = [self.fitness_function(individual) for individual in offspring]

            worst_offspring_index = offspring_fitnesses.index(max(offspring_fitnesses))

            best_parent_index = fitness.index(min(fitness))

            offspring[worst_offspring_index] = population[best_parent_index]

            offspring_fitnesses[worst_offspring_index] = fitness[best_parent_index]

            population = offspring
            fitness = offspring_fitnesses

        return best_overall_individual, best_overall_fit
            
            
def run_experiment(filename, variable_param_name, param_values, n_runs=5, 
                static_params=None):
    
    config = {
        'n': DEF_N,
        'pop_size': DEF_POPULATION_SIZE,
        'p_c': DEF_CROSSOVER_PROB,
        'p_m': DEF_MUTATION_PROB,
        'gen_max': DEF_GEN_MAX,
        'tourn': DEF_TOURNAMENT_SIZE
    }
    
    if static_params:
        config.update(static_params)

    with open(filename, 'w', newline='') as f:
        writer = csv.writer(f)
        writer.writerow(['generation', 'run', 'parameter', 'mean_fitness', 'best_fitness'])

        for val in param_values:
            if variable_param_name == 'n': config['n'] = val
            elif variable_param_name == 'pop_size': config['pop_size'] = val
            elif variable_param_name == 'p_m': config['p_m'] = val
            elif variable_param_name == 'p_c': config['p_c'] = val
            elif variable_param_name == 'tourn': config['tourn'] = val
            elif variable_param_name == 'gen_max': config['gen_max'] = val
            elif variable_param_name == 'baseline': pass
            
            print(f"  Running for {variable_param_name} = {val}")

            for r in range(1, n_runs + 1):
                ea = EA(
                    n=config['n'], 
                    population_size=config['pop_size'], 
                    crossover_prob=config['p_c'], 
                    mutation_prob=config['p_m'], 
                    tournament_size=config['tourn']
                )
                
                param_label = val if variable_param_name != 'baseline' else 'baseline'
                
                best_ind, best_score = ea.run(config['gen_max'], r, param_label, writer)
                
                if variable_param_name == 'baseline' and r == 1:
                    print(f"\n--- Best Solution for N={val} (Score: {best_score}) ---")
                    print(f"Vector: {[x+1 for x in best_ind]}")

if __name__ == "__main__":
    run_experiment(filename='baseline_n.csv',variable_param_name='n',param_values=[5, 10, 50, 100],n_runs=1)
    run_experiment(filename='population.csv',variable_param_name='pop_size',param_values=[10, 50, 100, 200],n_runs=5)
    run_experiment(filename='generations.csv',variable_param_name='gen_max',param_values=[200, 500, 2000, 5000, 10000],n_runs=5)
    run_experiment(filename='mutation.csv',variable_param_name='p_m',param_values=[0.0, 0.01, 0.05, 0.1, 0.5],n_runs=5)
    run_experiment(filename='crossover.csv',variable_param_name='p_c',param_values=[0.1, 0.3, 0.5, 0.7, 0.9],n_runs=5)
    run_experiment(filename='tournament.csv',variable_param_name='tourn',param_values=[2, 10, 25, 40, 50],n_runs=5)