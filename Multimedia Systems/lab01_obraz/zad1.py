import numpy as np
import matplotlib.pylab as plt
import cv2

images = ['A1.png','A2.jpg','A3.png','A4.jpg']

# cz. 1

def checker(name):
    print("------plt------")
    img1 = plt.imread(name)
    print(name)
    print(img1.dtype)
    print(img1.shape)
    print(np.min(img1),np.max(img1))
    print("------OpenCV------")
    print(name)
    img1 = cv2.imread(name)
    print(img1.dtype)
    print(img1.shape)
    print(np.min(img1),np.max(img1))

def imgToUInt8(img):
    if not isinstance(img,np.integer):
        img = np.astype(img*255,'uint8')
    return img

def imgToFloat(img):
    if not isinstance(img,np.float32):
        img = img/255.0
    return img

for image in images:
    img = cv2.imread(image)
    imgToFloat(img)
    imgToUInt8(img)


# cz.2
img = plt.imread('A1.png')
plt.imshow(img)
R=img[:,:,0]
plt.imshow(R)
plt.show()