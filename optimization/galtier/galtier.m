k = 6;
m = 2^k;
n = 70;
M = 35*m;

HM = bigH(M, M, n);

z0 = 0;
z = zeros(1, m);
for j = 1 : m-1
    
    
    i = 0;
    while( bigH(i, M, n)/HM < j/m )
        i = i+1;
    end
    z(j) = 1/M * i;    
end
z(end) = 1;

z = [z0 z];
estim = 0;
for j = 2:m
    differ = z(j) - z(j-1);
    val = n* (z(j-1))^(n-1);
    estim = estim + differ*val;
end


estim = 1 - estim
p_coll = 2/m * (n-1)/n


save galtier.mat z estim p_coll n
