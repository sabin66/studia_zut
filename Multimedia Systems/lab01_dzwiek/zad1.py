import numpy as np
import matplotlib.pyplot as plt
import sounddevice as sd
import soundfile as sf

data, fs = sf.read('sound1.wav',dtype='float32')
print(data.dtype)
print(data.shape)
print(fs)

# zad 1
time = np.arange(data.shape[0]) / fs
if data.shape[1] == 2:
    sound_L = data[:, 0]
    sound_R = data[:, 1]

    sound_mix = (sound_L + sound_R) / 2

    sf.write('sound_L.wav',sound_L,fs)
    sf.write('sound_R.wav',sound_R,fs)
    sf.write('sound_mix.wav',sound_mix,fs)

plt.figure(figsize=(10,8))
plt.subplot(3,1,1)
plt.plot(time,sound_L)
plt.xlabel('Czas [s]')
plt.ylabel('Amplituda')
plt.title('sound_L')

plt.subplot(3,1,2)
plt.plot(time,sound_R)
plt.xlabel('Czas [s]')
plt.ylabel('Amplituda')
plt.title('sound_R')

plt.subplot(3,1,3)
plt.plot(time,sound_mix)
plt.xlabel('Czas [s]')
plt.ylabel('Amplituda')
plt.title('sound_mix')
plt.show()
