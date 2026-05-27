import cv2
import numpy as np
import matplotlib.pyplot as plt
import os
from skimage.metrics import structural_similarity as ssim

# funkcje print_table plot_metrics przy pomocy gemini

OUTPUT_DIR = "wyniki"
os.makedirs(OUTPUT_DIR, exist_ok=True)
IMG_DIR = "obrazy_testowe"
os.makedirs(IMG_DIR, exist_ok=True)
JPEG_QUALITIES = [10, 20, 30, 40, 50, 60, 70, 75]
BLUR_SIZES = [3, 5, 7, 9, 11, 15, 19, 23]
NOISE_ALPHAS = [0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.8, 1.0]
METRIC_NAMES = ['MSE', 'NMSE', 'PSNR', 'IF', 'SSIM']


def calc_MSE(original, modified):
    orig = original.astype(np.float64)
    mod = modified.astype(np.float64)
    return np.mean((orig - mod) ** 2)


def calc_NMSE(original, modified):
    orig = original.astype(np.float64)
    mod = modified.astype(np.float64)
    numerator = np.sum((orig - mod) ** 2)
    denominator = np.sum(mod ** 2)
    if denominator == 0:
        return 0.0
    return numerator / denominator


def calc_PSNR(original, modified, max_val=255):
    mse = calc_MSE(original, modified)
    if mse == 0:
        return float('inf')
    return 10 * np.log10((max_val ** 2) / mse)


def calc_IF(original, modified):
    orig = original.astype(np.float64)
    mod = modified.astype(np.float64)
    numerator = np.sum((mod - orig) ** 2)
    denominator = np.sum(mod * orig)
    if denominator == 0:
        return 0.0
    return 1 - numerator / denominator


def calc_SSIM(original, modified):
    orig_gray = cv2.cvtColor(original, cv2.COLOR_BGR2GRAY)
    mod_gray = cv2.cvtColor(modified, cv2.COLOR_BGR2GRAY)
    return ssim(orig_gray, mod_gray)


def calc_all_metrics(original, modified):
    return {
        'MSE': calc_MSE(original, modified),
        'NMSE': calc_NMSE(original, modified),
        'PSNR': calc_PSNR(original, modified),
        'IF': calc_IF(original, modified),
        'SSIM': calc_SSIM(original, modified)
    }

def jpeg_compression(img, quality):
    encode_param = [int(cv2.IMWRITE_JPEG_QUALITY), quality]
    _, encimg = cv2.imencode('.jpg', img, encode_param)
    decimg = cv2.imdecode(encimg, 1)
    return decimg


def gaussian_blur(img, ksize):
    return cv2.GaussianBlur(img, (ksize, ksize), 0)


def gaussian_noise(img, alpha):
    sigma = 25
    gauss = np.random.normal(0, sigma, img.shape)
    noisy = (img + alpha * gauss).clip(0, 255).astype(np.uint8)
    return noisy

def run_experiment(img, img_name, method_name, params, func, param_label):
    results = []
    for p in params:
        degraded = func(img.copy(), p)
        metrics = calc_all_metrics(img, degraded)
        metrics['param'] = p
        results.append(metrics)
        fname = f"{img_name}_{method_name}_{p}.png"
        cv2.imwrite(os.path.join(OUTPUT_DIR, fname), degraded)

    return results


def plot_metrics(results, params, method_name, img_name, param_label):
    fig, axes = plt.subplots(2, 3, figsize=(15, 10))
    fig.suptitle(f'{method_name} - {img_name}', fontsize=14)

    for idx, metric in enumerate(METRIC_NAMES):
        ax = axes[idx // 3][idx % 3]
        values = [r[metric] for r in results]
        ax.plot(params, values, 'bo-', linewidth=2, markersize=6)
        ax.set_xlabel(param_label)
        ax.set_ylabel(metric)
        ax.set_title(metric)
        ax.grid(True)

    axes[1][2].set_visible(False)

    plt.tight_layout()
    fname = f"wykres_{img_name}_{method_name}.png"
    plt.savefig(os.path.join(OUTPUT_DIR, fname), dpi=100)
    plt.close()
    print(f"  Zapisano wykres: {fname}")


def print_table(results):
    header = f"{'Parametr':>10}"
    for m in METRIC_NAMES:
        header += f" | {m:>12}"
    print(header)
    print("-" * len(header))
    for r in results:
        line = f"{r['param']:>10}"
        for m in METRIC_NAMES:
            val = r[m]
            if m == 'PSNR' and val == float('inf'):
                line += f" | {'inf':>12}"
            else:
                line += f" | {val:>12.4f}"
        print(line)
    print()


if __name__ == "__main__":
    names = ['obraz1', 'obraz2', 'obraz3']
    images = [cv2.imread(os.path.join(IMG_DIR, f"{n}.png")) for n in names]

    experiments = [
        ((names[0], images[0]), "JPEG", JPEG_QUALITIES, jpeg_compression, "Jakość JPEG"),
        ((names[1], images[1]), "GaussianBlur", BLUR_SIZES, gaussian_blur, "Rozmiar filtra"),
        ((names[2], images[2]), "GaussianNoise", NOISE_ALPHAS, gaussian_noise, "Alpha"),
    ]

    all_results = {}

    for (img_name, img), method_name, params, func, param_label in experiments:
        print(f"Eksperyment: {method_name} na {img_name}")

        results = run_experiment(img, img_name, method_name, params, func, param_label)
        all_results[(img_name, method_name)] = results

        print(f"\nTabela wyników - {method_name} ({param_label}):")
        print_table(results)

        plot_metrics(results, params, method_name, img_name, param_label)

