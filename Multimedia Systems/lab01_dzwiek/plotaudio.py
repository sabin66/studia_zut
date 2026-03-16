import numpy as np
import matplotlib.pyplot as plt
import scipy.fftpack
import soundfile as sf


def plotAudio(signal,timeMargin=[0,0.02]):

    data,fs = sf.read(signal,dtype=np.int32)

    n = data.shape[0]

    time = np.arange(n) / fs

    yf = scipy.fftpack.fft(data)

    yf_half = yf[:(n//2)]

    freqs = np.linspace(0.0, (fs/2.0), (n//2))

    yf_db = 20 * np.log10(np.maximum(np.abs(yf_half),1e-10))

    plt.figure(figsize=(10, 6))
    
    plt.subplot(2, 1, 1)
    plt.plot(time, data)
    plt.title('Sygnał w dziedzinie czasu')
    plt.xlabel('Czas [s]')
    plt.ylabel('Amplituda')
    plt.xlim(timeMargin)
    plt.grid(True)
    
    plt.subplot(2, 1, 2)
    plt.plot(freqs, yf_db)
    plt.title('Widmo amplitudowe sygnału')
    plt.xlabel('Częstotliwość [Hz]')
    plt.ylabel('Amplituda [dB]')

    plt.xlim(0, fs/2) 
    plt.grid(True)
    plt.show()

if __name__ == '__main__':
    plotAudio('sin_440Hz.wav')