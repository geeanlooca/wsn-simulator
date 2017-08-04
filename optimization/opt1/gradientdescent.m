close all;
set(0,'DefaultFigureWindowStyle','docked')
set(0,'DefaultTextInterpreter','latex')

p = linspace(0,1,256);
U = [5 10 20 40];

handles = [];
figure('Color', 'White');
hold on;
for i = 1 : length(U)
    u = U(i);
    p_opt = optimize_p(u);
    J_opt = costobj(p_opt,u);
    
    J = costobj(p, u);
    
    handles(i) = plot(p, J, 'k','linewidth',1, 'DisplayName', sprintf("$u=%d$", u));
    plot(p_opt, J_opt, '-r*','markersize', 12);
    %plot([0 1], [J_opt J_opt], '-g');
end
hold off;

xlim([0 1]);
ylim([0 10]);
set(gca,'TickLabelInterpreter','latex')
set(gca, 'FontSize', 12)
xlabel('$p_i$', 'FontSize', 14);
ylabel('$E[v]$', 'FontSize', 14);
box on;
grid on;
leg = legend(handles);
set(leg, 'location', 'best');
set(leg, 'interpreter', 'latex');
set(leg, 'fontsize', 14);
