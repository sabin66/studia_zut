import numpy as np
import matplotlib.pylab as plt

def show_plot_3x3(filepath):
    img = plt.imread(filepath)
    
    R = img[:,:,0]
    G = img[:,:,1]
    B = img[:,:,2]

    Y1 = 0.299 * R + 0.587 * G + 0.114 * B
    Y2 = 0.2126 * R + 0.7152 * G + 0.0722 * B

    img_R_color = img.copy()
    img_R_color[:,:,1] = 0
    img_R_color[:,:,2] = 0

    img_G_color = img.copy()
    img_G_color[:,:,0] = 0
    img_G_color[:,:,2] = 0

    img_B_color = img.copy()
    img_B_color[:,:,0] = 0
    img_B_color[:,:,1] = 0

    # jesli uint8 to 0, 255, jesli float32 to 0.0, 1.0
    if np.issubdtype(img.dtype, np.integer): 
        v_min, v_max = 0, 255
    else:
        v_min, v_max = 0.0, 1.0

    plt.figure(figsize=(10, 10))

    plt.subplot(3,3,1)
    plt.imshow(img)
    plt.title('O')

    plt.subplot(3,3,2)
    plt.imshow(Y1, cmap=plt.cm.gray, vmin=v_min, vmax=v_max)
    plt.title('Y1')

    plt.subplot(3,3,3)
    plt.imshow(Y2, cmap=plt.cm.gray, vmin=v_min, vmax=v_max)
    plt.title('Y2')

    plt.subplot(3,3,4)
    plt.imshow(R, cmap=plt.cm.gray, vmin=v_min, vmax=v_max)
    plt.title('R (Gray)')

    plt.subplot(3,3,5)
    plt.imshow(G, cmap=plt.cm.gray, vmin=v_min, vmax=v_max)
    plt.title('G (Gray)')

    plt.subplot(3,3,6)
    plt.imshow(B, cmap=plt.cm.gray, vmin=v_min, vmax=v_max)
    plt.title('B (Gray)')

    plt.subplot(3,3,7)
    plt.imshow(img_R_color)
    plt.title('R (Color)')

    plt.subplot(3,3,8)
    plt.imshow(img_G_color)
    plt.title('G (Color)')

    plt.subplot(3,3,9)
    plt.imshow(img_B_color)
    plt.title('B (Color)')

    plt.show()

show_plot_3x3('B01.png')