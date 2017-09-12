k = 7;
m = 2^k;
n = 100;
M = 35*m;

HM = bigH(M, M, n);
H = zeros(M+1, 1);
for i = 1:M+1
    H(i) = bigH(i-1, M, n);
end

z0 = 0;
z = zeros(1, m);
for j = 1 : m-1        
    i = 0;
    while( H(i+1)/HM < j/m )
        i = i+1;
    end
    z(j) = 1/M * i;    
end
z(end) = 1;

z = [z0 z];
estim = 0;
for j = 2:m+1
    differ = z(j) - z(j-1);
    val = n* (z(j-1))^(n-1);
    estim = estim + differ*val;
end


estim = 1 - estim
p_coll = 2/m * (n-1)/n


save galtier.mat z estim p_coll n
