close all;
set(0,'DefaultFigureWindowStyle','docked')
set(0,'DefaultTextInterpreter','latex')


CWmin = 16;
CWmax = 1024;
m = log2(CWmax/CWmin);
tau = linspace(0,1,256);
p = linspace(0,1,256);


N = [2 5 10 20];
handles = []
figure;
hold on;
    for j = 1 : length(N)
        
        n = N(j);
        fun = @(x) DCF_system(x, n);
        x0 = [0.5, 0.5];
        x = fsolve(fun, x0);
        p_sol(j) = x(1);
        tau_sol(j) = x(2);
        
        P = 1 - (1-tau).^(n-1);
        TAU_star = 1-(1-p).^(1/(n-1));
        TAU = zeros(size(P));
        for i = 1 : length(p)
            TAU(i) = 2./(1 + CWmin + p(i).*CWmin .* sum( (2*p(i)).^(0:m-1)));
        end
        
        handles(2*j - 1) = plot(p, TAU, 'k');
        handles(2*j) = plot(P,tau, 'b-.');
        plot(x(1), x(2), 'r*');
    end    
hold off;
box on;
xlim([0 1]);
ylim([0 1]);
xlabel('$p$');
ylabel('$\tau$');
set(gca,'TickLabelInterpreter','latex')
l = legend(handles(1:2));
set(l, 'interpreter', 'latex');