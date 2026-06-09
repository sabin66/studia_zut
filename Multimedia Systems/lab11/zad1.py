import numpy as np
import cv2
from skimage.metrics import structural_similarity as ssim

def draw_line(img, x0, y0, x1, y1, color):
    dx = abs(x1 - x0)
    dy = abs(y1 - y0)
    sx = 1 if x0 < x1 else -1
    sy = 1 if y0 < y1 else -1
    err = dx - dy
    
    while True:
        if 0 <= x0 < img.shape[1] and 0 <= y0 < img.shape[0]:
            img[y0, x0] = color
        if x0 == x1 and y0 == y1:
            break
        e2 = 2 * err
        if e2 > -dy:
            err -= dy
            x0 += sx
        if e2 < dx:
            err += dx
            y0 += sy

def draw_circle(img, xc, yc, r, color):
    x = 0
    y = r
    d = 3 - 2 * r

    def draw_points(xc, yc, x, y, img, color):
        points = [
            (xc+x, yc+y), (xc-x, yc+y), (xc+x, yc-y), (xc-x, yc-y),
            (xc+y, yc+x), (xc-y, yc+x), (xc+y, yc-x), (xc-y, yc-x)
        ]
        for px, py in points:
            if 0 <= px < img.shape[1] and 0 <= py < img.shape[0]:
                img[py, px] = color

    while y >= x:
        draw_points(xc, yc, x, y, img, color)
        x += 1
        if d > 0:
            y -= 1
            d = d + 4 * (x - y) + 10
        else:
            d = d + 4 * x + 6

def draw_polygon(img, points, color):
    for i in range(len(points)):
        p1 = points[i]
        p2 = points[(i + 1) % len(points)]
        draw_line(img, p1[0], p1[1], p2[0], p2[1], color)

def fill_polygon(img, points, color):
    ymin = max(0, min(p[1] for p in points))
    ymax = min(img.shape[0]-1, max(p[1] for p in points))
    xmin = max(0, min(p[0] for p in points))
    xmax = min(img.shape[1]-1, max(p[0] for p in points))
    
    for y in range(ymin, ymax+1):
        for x in range(xmin, xmax+1):
            inside = False
            j = len(points) - 1
            for i in range(len(points)):
                xi, yi = points[i]
                xj, yj = points[j]
                if (yi > y) != (yj > y):
                    intersect_x = (xj - xi) * (y - yi) / (yj - yi + 1e-6) + xi
                    if x < intersect_x:
                        inside = not inside
                j = i
            if inside:
                img[y, x] = color

def fill_circle(img, xc, yc, r, color):
    for y in range(max(0, yc-r), min(img.shape[0], yc+r+1)):
        for x in range(max(0, xc-r), min(img.shape[1], xc+r+1)):
            if (x - xc)**2 + (y - yc)**2 <= r**2:
                img[y, x] = color

def generate_image(width, height, vector_dict):
    img = np.zeros((height, width, 3), dtype=np.uint8)
    for obj in vector_dict['objects']:
        color = obj['color']
        if obj['type'] == 'circle':
            xc = int(obj['center'][0] * width)
            yc = int(obj['center'][1] * height)
            r = int(obj['radius'] * min(width, height))
            fill_circle(img, xc, yc, r, color)
        elif obj['type'] == 'polygon':
            pts = [(int(p[0] * width), int(p[1] * height)) for p in obj['points']]
            fill_polygon(img, pts, color)
    return img

vector_image = {
    "objects": [
        {"type": "circle", "center": (0.2, 0.2), "radius": 0.15, "color": (0, 0, 200)}, 
        {"type": "polygon", "points": [(0.1, 0.1), (0.3, 0.1), (0.2, 0.3)], "color": (200, 0, 0)},
        
        {"type": "polygon", "points": [(0.4, 0.1), (0.7, 0.1), (0.7, 0.3), (0.4, 0.3)], "color": (150, 150, 150)},
        {"type": "polygon", "points": [(0.45, 0.15), (0.5, 0.15), (0.5, 0.2), (0.45, 0.2)], "color": (50, 50, 50)},
        {"type": "polygon", "points": [(0.6, 0.15), (0.65, 0.15), (0.65, 0.2), (0.6, 0.2)], "color": (50, 50, 50)},
        
        {"type": "polygon", "points": [(0.8, 0.1), (0.85, 0.1), (0.85, 0.25), (0.95, 0.25), (0.95, 0.3), (0.8, 0.3)], "color": (0, 255, 255)},
        
        {"type": "circle", "center": (0.2, 0.6), "radius": 0.1, "color": (0, 255, 255)},
        {"type": "polygon", "points": [(0.2, 0.5), (0.4, 0.5), (0.4, 0.7), (0.2, 0.7)], "color": (0, 75, 150)},
        
        {"type": "polygon", "points": [(0.6, 0.5), (0.8, 0.5), (0.7, 0.8)], "color": (128, 0, 0)},
        {"type": "polygon", "points": [(0.55, 0.6), (0.85, 0.6), (0.85, 0.7), (0.55, 0.7)], "color": (0, 255, 0)},
        {"type": "polygon", "points": [(0.45, 0.55), (0.60, 0.55), (0.525, 0.75)], "color": (255, 255, 255)},
    ]
}

sizes = [(100, 100), (200, 200), (300, 300), (400, 400), (500, 500)]
images = [generate_image(w, h, vector_image) for w, h in sizes]

target_size = (300, 300)
resized_images = [cv2.resize(img, target_size) for img in images]

img_ref = resized_images[2]

def calc_mse(img1, img2):
    return np.mean((img1.astype(float) - img2.astype(float)) ** 2)

print(f"Baza referencyjna: Obraz o rozmiarze natywnym {target_size}\n")
for i, img in enumerate(resized_images):
    mse_val = calc_mse(img_ref, img)
    ssim_val = ssim(img_ref, img, channel_axis=2)
    print(f"Obraz (zródłowy: {sizes[i]}) -> przeskalowany do {target_size}: MSE = {mse_val:.2f}, SSIM = {ssim_val:.4f}")
    cv2.imwrite(f"output_{sizes[i][0]}x{sizes[i][1]}.png", img)