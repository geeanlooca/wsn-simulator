% obtaining collision rates in Table 1 of 'A Medium Access Control Scheme for Wireless
% LANs with Constant-Time Contention' (abichar2011). The example given in
% Section 3.5 using an optimized probability vector p for k = 5 is also
% tested.

runs = 10000;
collisions_unif = 0;
collisions_opt = 0;
n = 25;
k = 5;

p_unif = 0.5*ones(1, k);
if (k == 5)
    p_opt = [0.2563, 0.36715, 0.4245, 0.4314, 0.5];
end

for i = 1 : runs
    collisions_unif = collisions_unif + CONTI(n,k,p_unif);
    if (k == 5)
        collisions_opt = collisions_opt + CONTI(n,k,p_opt);
    end
end

collision_rate_unif = collisions_unif/runs*100;
fprintf('[n = %d, k = %d] Uniform probability vector: %.2f\n', ...
    n,k,collision_rate_unif);

if (k == 5)
    collision_rate_opt = collisions_opt/runs*100;
    fprintf('[n = %d, k = %d] Optimized probability vector: %.2f\n', ...
    n,k,collision_rate_opt);
end
