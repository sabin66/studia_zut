import numpy as np
R=np.random.rand(5,5)
A=np.zeros(R.shape)
B=np.zeros(R.shape)
C=np.zeros(R.shape)


idx=R<0.25
A[idx]=1 # <-
B[idx]+=0.25 # <-
C[idx]=2*R[idx]-0.25 # <-
C[np.logical_not(idx)]=4*R[np.logical_not(idx)]-0.5 # <-
print(R)
print(A)
print(B)
print(C)