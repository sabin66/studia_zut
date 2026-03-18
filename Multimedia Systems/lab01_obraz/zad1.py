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
    if not np.issubdtype(img.dtype,np.unsignedinteger):
        img = (img*255).astype('uint8')
    return img

def imgToFloat(img):
    if not np.issubdtype(img.dtype,np.floating):
        img = img/255.0
    return img

for image in images:
    img = cv2.imread(image)
    imgToFloat(img)
    imgToUInt8(img)


# cz.2
img = plt.imread('B01.png')
plt.imshow(img)
R=img[:,:,0]
G=img[:,:,1]
B=img[:,:,2]
Y2=0.2126 * R + 0.7152 * G + 0.0722 * B

if np.issubdtype(img.dtype,np.unsignedinteger): 
    plt.imshow(R,cmap=plt.cm.gray,vmin=0, vmax=255) # jesli uint8 to 0, 255, jesli float32 to 0.0, 1.0
else:
    plt.imshow(R,cmap=plt.cm.gray,vmin=0.0, vmax=1.0)
plt.show()

plt.figure()
plt.imshow(Y2, cmap=plt.cm.gray, vmin=0.0, vmax=1.0)
plt.title("Obraz w skali szarości (metoda z wagami)")
plt.show()

img_cv2 = cv2.imread('B01.png')
plt.figure()
plt.imshow(img_cv2)
plt.title("OpenCV (v1)")
plt.show()

img_RGB = cv2.cvtColor(img_cv2, cv2.COLOR_BGR2RGB)
img_BGR = cv2.cvtColor(img_RGB, cv2.COLOR_RGB2BGR)

plt.figure()
plt.imshow(img_RGB)
plt.title("OpenCV (RGB)")
plt.show()

plt.figure()
plt.imshow(img_BGR)
plt.title("OpenCV (BGR)")
plt.show()
