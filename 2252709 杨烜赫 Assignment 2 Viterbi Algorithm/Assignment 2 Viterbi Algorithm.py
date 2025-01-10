# 2252709 Xuanhe Yang
import numpy as np

# Define states and observations
states = ['good', 'neutral', 'bad']
observations = ['A', 'C', 'B', 'A', 'C']
# Set NumPy print options to suppress scientific notation
np.set_printoptions(suppress=True, floatmode='fixed')
# Transition probability matrix
transition_prob = np.array([
    [0.2, 0.3, 0.5],  # From good
    [0.2, 0.2, 0.6],  # From neutral
    [0.0, 0.2, 0.8]   # From bad
])

# Emission probability matrix
emit_prob = np.array([
    [0.7, 0.2, 0.1],  # P(A|good), P(B|good), P(C|good)
    [0.3, 0.4, 0.3],  # P(A|neutral), P(B|neutral), P(C|neutral)
    [0.0, 0.1, 0.9]   # P(A|bad), P(B|bad), P(C|bad)
])

def viterbi(_obs, _states, _start_prob, _trans_prob, _emit_prob):
    n_states = len(_states)  # Number of states
    n_obs = len(_obs)        # Number of observations

    # Initialize the probability matrix
    viterbi_matrix = np.zeros((n_states, n_obs))
    path = np.zeros((n_states, n_obs), dtype=int)

    # Initialize the first column of the Viterbi matrix
    for i in range(n_states):
        viterbi_matrix[i][0] = _start_prob[i] * _emit_prob[i][_obs[0]]
        path[i][0] = 0  # Starting point for paths

    print("Initial Viterbi matrix:")
    print(viterbi_matrix)

    # Dynamic programming to compute subsequent columns
    for t in range(1, n_obs):
        for j in range(n_states):
            max_prob = -1
            max_state = -1
            for i in range(n_states):
                # Calculate the probability for state j at time t
                prob = (viterbi_matrix[i][t - 1] *
                        _trans_prob[i][j] *
                        _emit_prob[j][_obs[t]])
                print(f"Calculating P(state={_states[j]}, time={t}):")
                print(f"  P(previous state={_states[i]}) * P({_states[i]} -> {_states[j]}) * P({_obs[t]}|{_states[j]})")
                print(f"  = {viterbi_matrix[i][t - 1]:.7f} * {_trans_prob[i][j]:.7f} * {_emit_prob[j][_obs[t]]:.7f} = {prob:.7f}")

                if prob > max_prob:
                    max_prob = prob
                    max_state = i  # Store the state that gives the maximum probability
            viterbi_matrix[j][t] = max_prob
            path[j][t] = max_state  # Store the best previous state

        print(f"After column {t}, Viterbi matrix:")
        print(viterbi_matrix)

    # Backtrack to find the best path
    result_path = []
    max_final_prob = -1
    max_final_state = -1

    # Find the state with the maximum probability at the final time step
    for i in range(n_states):
        if viterbi_matrix[i][n_obs - 1] > max_final_prob:
            max_final_prob = viterbi_matrix[i][n_obs - 1]
            max_final_state = i

    result_path.append(max_final_state)  # Add the best final state to the path
    print(f"Final state with max probability: state={_states[max_final_state]}, probability={max_final_prob:.7f}")

    # Backtracking through the path
    for t in range(n_obs - 1, 0, -1):
        prev_state = path[result_path[0]][t]
        result_path.insert(0, prev_state)  # Insert the best previous state
        print(f"Backtracking step at time {t}: state={_states[prev_state]}")

    return result_path, max_final_prob  # Return the best path and its probability

# Initial probabilities, assuming equal initial probabilities for all states
start_prob = np.array([1/3, 1/3, 1/3])

# Convert observation sequence to indices
obs_indices = [0, 2, 1, 0, 2]  # A=0, B=1, C=2

# Execute the Viterbi algorithm
best_path, best_prob = viterbi(obs_indices, states, start_prob, transition_prob, emit_prob)

# Convert state indices back to state names
best_path_states = [states[i] for i in best_path]

print("Best path:", best_path_states)
print(f"Best path probability: {best_prob:.7f}")
