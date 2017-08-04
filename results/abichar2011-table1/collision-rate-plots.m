close all;
set(0,'DefaultFigureWindowStyle','docked')
set(0,'DefaultTextInterpreter','latex')

runs = 10000;
N = [10 15 25 35];
K = 2:9;
collision_rate = zeros(length(K), length(N));
p_opt = [0.2563, 0.36715, 0.4245, 0.4314, 0.5];
errors = zeros(size(collision_rate));

for k = 1 : length(K)    
    for n = 1 : length(N)
        
%         
%         col_rate = mean(collision_estimates);
%         col_std = std(collision_estimates);
%         q = quantile(collision_estimates, 0.9);
%         errors(k, n) = q*col_std/sqrt(runs);
        
        collision_rate(k,n) = estimate_collisionrate(N(n), K(k), ...
            0.5*ones(1, K(k)), runs);
    end
    
        
%     x = k + randn(1, runs);
%     x_mean = mean(x);
%     X_mean(k) = x_mean;
%     x_std = std(x);
%     q = quantile(x, 0.975);
%     errors(k) = q*x_std/sqrt(runs);    
end

styles = {'-kx', '--ro', '-.bs', '--g^'};
marks = {'black', 'red', 'black', 'green'};
figure('Color', 'White');
hold on;
for n = 1 : length(N)
    plot(K, collision_rate(:, n), styles{n}, 'linewidth', 1, ...
        'MarkerFaceColor', marks{n}, 'MarkerSize', 8, ...
        'DisplayName', sprintf("$n = %d$", N(n)));
end
hold off;

set(gca,'TickLabelInterpreter','latex')
set(gca, 'FontSize', 12)
xlabel('$k$', 'FontSize', 14);
ylabel('Collision rate', 'FontSize', 14);
box on;
grid on;
grid minor;
xlim([1.5 9.5]);
ylim([-0.1, 1.1]);
leg = legend('location', 'best');
set(leg, 'interpreter', 'latex');
set(leg, 'fontsize', 14);


csvwrite('out.csv', collision_rate);


%%
p_opt = [0.2563, 0.36715, 0.4245, 0.4314, 0.5];
N = 5:5:30;
col_uni = zeros(1, length(N));
col_opt = zeros(size(col_uni));
for n = 1 : length(N)
    col_uni(n) = estimate_collisionrate(N(n), 5, 0.5*ones(1, N(n)), 100000);
    col_opt(n) = estimate_collisionrate(N(n), 5, p_opt, 100000);
end

figure('Color', 'White');
hold on;
    plot(N, col_uni, '-ko', 'MarkerFaceColor', 'black', 'MarkerSize', 8, ...
        'DisplayName', 'Equiprobable');
    plot(N, col_opt, '--r^', 'MarkerFaceColor', 'red', 'MarkerSize', 8, ...
        'DisplayName', 'Optimal');
    plot(N, col_uni./col_opt, '--r^', 'MarkerFaceColor', 'red', 'MarkerSize', 8, ...
        'DisplayName', 'Gain');
hold off;
set(gca,'TickLabelInterpreter','latex')
set(gca, 'FontSize', 12)
xlabel('$n$', 'FontSize', 14);
ylabel('Collision rate', 'FontSize', 14);
box on;
grid on;
grid minor;
xlim([4 31]);
ylim([0.025, 0.425]);
leg = legend('location', 'best');
set(leg, 'interpreter', 'latex');
set(leg, 'fontsize', 14);
