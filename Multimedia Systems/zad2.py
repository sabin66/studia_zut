import numpy as np
import matplotlib.pyplot as plt
import scipy.fftpack
import sounddevice as sd
import soundfile as sf

data, fs = sf.read('sin_440Hz.wav',dtype=np.int32)

# plt.figure()
# plt.subplot(2,1,1)
# plt.plot(np.arange(0,data.shape[0])/fs,data)

# plt.subplot(2,1,2)
# yf = scipy.fftpack.fft(data)
# plt.plot(np.arange(0,fs,1.0*fs/(yf.size)),np.abs(yf))
# plt.show()

fsize = 2**8
plt.figure()
plt.subplot(2,1,1)
plt.plot(np.arange(0,data.shape[0])/fs,data)
plt.subplot(2,1,2)
yf = scipy.fftpack.fft(data,fsize)
plt.plot(np.arange(0,fs/2,fs/fsize),20*np.log10( np.abs(yf[:fsize//2])))
plt.show()