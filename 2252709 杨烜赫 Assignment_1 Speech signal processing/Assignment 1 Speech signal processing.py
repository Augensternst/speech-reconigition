import os
import numpy as np
import librosa
import matplotlib.pyplot as plt
from scipy.fftpack import dct

# Get the directory where the Python file is located
current_dir = os.path.dirname(os.path.abspath(__file__))
audio_file = os.path.join(current_dir, "test.wav")

# Load a sample speech file
signal, sample_rate = librosa.load(audio_file, sr=None)

# Step 1: Pre-emphasis
pre_emphasis = 0.97
emphasized_signal = np.append(signal[0], signal[1:] - pre_emphasis * signal[:-1])

# Step 2: Framing and Windowing
frame_size = 0.025  # 25 ms
frame_stride = 0.01  # 10 ms
frame_length, frame_step = frame_size * sample_rate, frame_stride * sample_rate  # Convert from seconds to samples
frame_length = int(round(frame_length))
frame_step = int(round(frame_step))
signal_length = len(emphasized_signal)
num_frames = int(np.ceil(float(np.abs(signal_length - frame_length)) / frame_step))  # Make sure to not miss any samples

# Pad Signal to make sure that all frames have equal number of samples
pad_signal_length = num_frames * frame_step + frame_length
z = np.zeros((pad_signal_length - signal_length))
pad_signal = np.append(emphasized_signal, z)  # Pad Signal

# Extract frames from the signal
indices = np.tile(np.arange(0, frame_length), (num_frames, 1)) + np.tile(np.arange(0, num_frames * frame_step, frame_step), (frame_length, 1)).T
frames = pad_signal[indices.astype(np.int32, copy=False)]

# Step 3: Apply Hamming window to each frame
frames *= np.hamming(frame_length)

# Step 4: Compute STFT (Short Time Fourier Transform) using FFT
NFFT = 512
mag_frames = np.absolute(np.fft.rfft(frames, NFFT))  # Magnitude of the FFT
pow_frames = ((1.0 / NFFT) * (mag_frames ** 2))  # Power Spectrum

# Step 5: Apply Mel Filterbank
nfilt = 40
low_freq_mel = 0
high_freq_mel = 2595 * np.log10(1 + (sample_rate / 2) / 700)  # Convert Hz to Mel
mel_points = np.linspace(low_freq_mel, high_freq_mel, nfilt + 2)  # Equally spaced in Mel scale
hz_points = 700 * (10**(mel_points / 2595) - 1)  # Convert Mel to Hz
bin = np.floor((NFFT + 1) * hz_points / sample_rate).astype(int)

fbank = np.zeros((nfilt, int(np.floor(NFFT / 2 + 1))))
for m in range(1, nfilt + 1):
    f_m_minus = int(bin[m - 1])  # left
    f_m = int(bin[m])            # center
    f_m_plus = int(bin[m + 1])   # right

    for k in range(f_m_minus, f_m):
        fbank[m - 1, k] = (k - bin[m - 1]) / (bin[m] - bin[m - 1])
    for k in range(f_m, f_m_plus):
        fbank[m - 1, k] = (bin[m + 1] - k) / (bin[m + 1] - bin[m])

filter_banks = np.dot(pow_frames, fbank.T)
filter_banks = np.where(filter_banks == 0, np.finfo(float).eps, filter_banks)  # Numerical Stability
filter_banks = 20 * np.log10(filter_banks)  # dB

# Step 6: Apply Discrete Cosine Transform (DCT)
num_ceps = 13
mfcc = dct(filter_banks, type=2, axis=1, norm='ortho')[:, :num_ceps]

# Step 7: Dynamic Feature Extraction (Delta and Delta-Delta)
delta_mfcc = librosa.feature.delta(mfcc)
delta2_mfcc = librosa.feature.delta(mfcc, order=2)

# Step 8: Feature Transformation (Mean Normalization)
mfcc -= (np.mean(mfcc, axis=0) + 1e-8)

# Create subplots
fig, axs = plt.subplots(1, 2, figsize=(15, 6))

# Plot the custom MFCC extraction result
axs[0].imshow(mfcc.T, aspect='auto', origin='lower')
axs[0].set_title('Custom MFCC')

# Comparison with librosa's built-in MFCC extraction
librosa_mfcc = librosa.feature.mfcc(y=signal, sr=sample_rate, n_mfcc=num_ceps)
axs[1].imshow(librosa_mfcc, aspect='auto', origin='lower')
axs[1].set_title('MFCC (Librosa)')

# Adjust layout
plt.tight_layout()
plt.show()
