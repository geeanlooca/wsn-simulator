k = 6;
m = 2^k;
n = 1:100;
bound = @(n) 2/m * (n-1)/n;
p_coll = zeros(size(n));

for i=1:length(n)
    p_coll(i) = bound(n(i));
end

plot(n, p_coll);