import os
import pickle
import numpy as np
import scipy.io.wavfile as wvf
from python_speech_features import mfcc
from hmmlearn.hmm import GMMHMM
import heapq
import matplotlib.pyplot as plt
import scipy

# Set matplotlib to use a font that supports Chinese to prevent garbled text
plt.rcParams['font.sans-serif'] = ['SimHei']
plt.rcParams['axes.unicode_minus'] = False

# Training data path
train_data_path = os.path.join(os.getcwd(), "datas/train/speech")
label_path = os.path.join(os.getcwd(), "datas/labels/trainprompts_m")
# Test data path
test_data_path = os.path.join(os.getcwd(), "datas/test/speech")
# Model save path
model_path = "hmm_gmm_model.pkl"


def extract_mfcc_features(signal, sample_rate, num_ceps=13, n_fft=512,
                          frame_size=0.025, frame_stride=0.01, nfilt=26,
                          cep_lifter=22, low_freq=0, high_freq=None,
                          pre_emphasis=0.97, append_energy=True):
    # Pre-emphasis
    emphasized_signal = np.append(signal[0], signal[1:] - pre_emphasis * signal[:-1])

    # Framing
    frame_length = int(round(frame_size * sample_rate))
    frame_step = int(round(frame_stride * sample_rate))
    signal_length = len(emphasized_signal)
    num_frames = int(np.ceil(float(np.abs(signal_length - frame_length)) / frame_step))

    pad_signal_length = num_frames * frame_step + frame_length
    z = np.zeros((pad_signal_length - signal_length))
    padded_signal = np.append(emphasized_signal, z)

    indices = (np.tile(np.arange(0, frame_length), (num_frames, 1)) +
               np.tile(np.arange(0, num_frames * frame_step, frame_step), (frame_length, 1)).T)
    frames = padded_signal[indices.astype(np.int32, copy=False)]

    # Apply Hamming window
    frames *= np.hamming(frame_length)

    # Compute power spectrum
    mag_frames = np.absolute(np.fft.rfft(frames, n_fft))
    pow_frames = ((1.0 / n_fft) * (mag_frames ** 2))

    # Construct Mel-filter bank
    if high_freq is None:
        high_freq = sample_rate / 2
    low_freq_mel = 2595 * np.log10(1 + low_freq / 700)
    high_freq_mel = 2595 * np.log10(1 + high_freq / 700)
    mel_points = np.linspace(low_freq_mel, high_freq_mel, nfilt + 2)
    hz_points = 700 * (10 ** (mel_points / 2595) - 1)
    bin = np.floor((n_fft + 1) * hz_points / sample_rate).astype(int)

    fbank = np.zeros((nfilt, int(np.floor(n_fft / 2 + 1))))
    for m in range(1, nfilt + 1):
        f_m_minus = bin[m - 1]
        f_m = bin[m]
        f_m_plus = bin[m + 1]
        for k in range(f_m_minus, f_m):
            fbank[m - 1, k] = (k - bin[m - 1]) / (bin[m] - bin[m - 1])
        for k in range(f_m, f_m_plus):
            fbank[m - 1, k] = (bin[m + 1] - k) / (bin[m + 1] - bin[m])

    # Apply filter bank to power spectra
    filter_banks = np.dot(pow_frames, fbank.T)
    filter_banks = np.where(filter_banks == 0, np.finfo(float).eps, filter_banks)
    filter_banks = np.log(filter_banks)

    # Apply DCT to get MFCC
    mfcc = scipy.fftpack.dct(filter_banks, type=2, axis=1, norm='ortho')[:, :num_ceps]

    # Replace first coefficient with log energy
    if append_energy:
        frame_energies = np.sum(pow_frames, axis=1)  # Frame energy
        frame_energies = np.where(frame_energies == 0, np.finfo(float).eps, frame_energies)
        mfcc[:, 0] = np.log(frame_energies)

    # Apply sinusoidal liftering
    n = np.arange(num_ceps)
    lift = 1 + (cep_lifter / 2) * np.sin(np.pi * n / cep_lifter)
    mfcc *= lift

    return mfcc


def wav2mfcc(labels, data_paths):
    """
    Convert WAV files to MFCC features
    :param labels: List of speech labels
    :param data_paths: List of speech data paths
    :return: Dictionary of MFCC features for each label
    """
    trng_data = {}
    for label, data_path in zip(labels, data_paths):
        mfccs = []
        rate, sig = wvf.read(data_path)
        # mfcc_feat = mfcc(sig, rate)  # Extract MFCC features
        mfcc_feat = extract_mfcc_features(sig, rate)
        mfccs.append(mfcc_feat)
        trng_data[label] = mfccs
        print(f"Extracted MFCC features for label {label}")
    return trng_data


def obtain_config(labels):
    """
    Generate the HMM-GMM configuration dictionary for each label (sets HMM component and GMM mixture count).
    :param labels: List of labels
    :return: Configuration dictionary for each label
    """
    conf = {}
    for label in labels:
        conf[label] = {}
        conf[label]["n_components"] = 2  # Number of HMM states (e.g., 2 states for start and end states)
        conf[label]["n_mix"] = 2         # Number of GMM mixtures (use 2 GMM components per HMM state)
    return conf


def get_hmm_gmm(trng_datas=None, GMM_configs=None, model_path="hmm_gmm_model.pkl", from_file=False):
    """
    Train an HMM-GMM model or load a pre-trained model from a file.
    :param trng_datas: Training data dictionary
    :param GMM_configs: Configuration dictionary for each label
    :param model_path: Path to save the model
    :param from_file: Whether to load the model from file
    :return: Trained HMM-GMM model dictionary
    """
    hmm_gmm = {}
    if not from_file:
        # Train the model if from_file is False
        for label, trng_data in trng_datas.items():
            GMM_config = GMM_configs[label]

            # Create HMM-GMM model
            hmm_gmm[label] = GMMHMM(
                n_components=GMM_config["n_components"],  # Number of HMM states
                n_mix=GMM_config["n_mix"],  # Number of GMM mixtures
                covariance_type='diag',  # Diagonal covariance matrix
                n_iter=100,  # Maximum number of iterations
                verbose=True  # Print training process
            )

            if trng_data:
                # Train the model with the data, the data must be a 2D array with shape (n_samples, n_features)
                hmm_gmm[label].fit(np.vstack(trng_data))  # Fit the model using training data

            print(f"Trained HMM-GMM model for label {label}")

        # Save the trained model
        pickle.dump(hmm_gmm, open(model_path, "wb"))
        print(f"Model saved to {model_path}")
    else:
        hmm_gmm = pickle.load(open(model_path, "rb"))
        print(f"Loaded pre-trained model from: {model_path}")

    return hmm_gmm


def train(train_data_path, label_path, model_path, save_intermediate=False):
    """
    Train the HMM-GMM model and optionally save intermediate results.
    :param train_data_path: Path to the training data folder
    :param label_path: Path to the label file
    :param model_path: Path to save the model
    :param save_intermediate: Whether to save the MFCC feature images
    :return: Trained HMM-GMM model
    """
    with open(os.path.join(label_path), encoding='utf-8') as f:
        labels = f.readlines()  # Read all lines, return as list
    data_paths = [train_data_path + '/' + line.split()[0] + '.wav' for line in labels]
    labels = [' '.join(line.split()[1:]).strip() for line in labels]
    train_datas = wav2mfcc(labels, data_paths)
    GMM_configs = obtain_config(labels)
    hmm_gmm = get_hmm_gmm(train_datas, GMM_configs, model_path)
    return hmm_gmm


# Train the model and save it
hmm_gmm = train(train_data_path, label_path, model_path)


def test_file(test_file, hmm_gmm):
    """
    Use the trained HMM-GMM model to predict the test file and return the prediction results.
    :param test_file: Test file path
    :param hmm_gmm: Trained HMM-GMM model dictionary
    :return: Return the top N prediction results and the score for each label
    """
    rate, sig = wvf.read(test_file)  # Read the test file
    # mfcc_feat = mfcc(sig, rate)  # Extract MFCC features
    mfcc_feat = extract_mfcc_features(sig, rate)
    pred = {}

    # Calculate the score of each model for the test file
    for model in hmm_gmm:
        pred[model] = hmm_gmm[model].score(mfcc_feat)

    # Save the MFCC feature image of the test file
    plt.figure(figsize=(10, 4))
    plt.imshow(mfcc_feat.T, cmap='jet', origin='lower', aspect='auto')
    plt.title(f'MFCC feature image of test file: {test_file}')
    plt.xlabel('Frame index')
    plt.ylabel('MFCC coefficients')
    plt.colorbar()
    mfcc_image_path = f"mfcc_test_{os.path.basename(test_file)}.png"
    plt.savefig(mfcc_image_path)
    plt.close()
    print(f"Saved MFCC feature image of the test file: {mfcc_image_path}")

    return get_nbest(pred, 2), pred


def get_nbest(d, n):
    """
    Get the top N prediction results from the scores.
    :param d: Prediction scores for each label
    :param n: Number of top results needed
    :return: Top N labels and their scores
    """
    return heapq.nlargest(n, d, key=lambda k: d[k])


def predict_label(file, hmm_gmm):
    """
    Use the trained HMM-GMM model to predict the given file.
    :param file: Test file path
    :param hmm_gmm: Trained HMM-GMM model dictionary
    :return: Prediction result and its probability
    """
    predicted = test_file(file, hmm_gmm)
    return predicted


# Test prediction
wave_path = os.path.join(test_data_path, "T0001.wav")
predicted, probs = predict_label(wave_path, hmm_gmm)
print(f"Prediction result: {predicted[0]} with score: {probs[predicted[0]]}")
