clear all;
close all;
set(0,'DefaultFigureWindowStyle','docked')
set(0,'DefaultTextInterpreter','latex')


CWmin = 16;
CWmax = 1024;
m = log2(CWmax/CWmin);

N = [2 3 4 5 7 9 10:5:50 60:10:100];
tau = zeros(size(N));
p = zeros(size(N));
ptr = zeros(size(N));
Ps = zeros(size(N));

for j = 1 : length(N)

    n = N(j);
    fun = @(x) DCF_system(x, n);
    x0 = [0.5, 0.5];
    x = fsolve(fun, x0);
    p(j) = x(1);
    tau(j) = x(2); 
    %p_th = 1 - (1-tau(j)).^(n-1);
    ptr(j) = 1-(1-tau(j)).^n;
    Ps(j) = (n*tau(j)*(1-tau(j))^(n-1))/ptr(j);
end


figure;
hold on;
    plot(N, p, '-kx', 'DisplayName', '$p$');
    plot(N, tau, '--r^', 'DisplayName', '$\tau$');
    plot(N, Ps, '-.gs', 'DisplayName', '$P_{s}$');
    %plot(N, ptr, ':bo', 'DisplayName', '$p_{tr}$');
    %plot(N, , 'm', 'LineWidth', 2, 'DisplayName', '$p_{coll}$');
hold off;
box on;
xlabel('$n$');
ylabel('Probability');
set(gca,'TickLabelInterpreter','latex')
l = legend('show');
set(l, 'location', 'best');
set(l, 'interpreter', 'latex');