close all;
set(0,'DefaultFigureWindowStyle','docked')
clear all;

p = 0.5; % starting point
gamma = 1e-3; % learning rate
u = 20; % number of contending stations
stopping_th = 1e-5;

J = costobj(p, u); % compute initial value of cost function

stop = false;
iter = 0; % number of iterations
max_iter = 1500; % maximum number of iterations allowed

while stop==false && iter < max_iter
    
    % compute gradient
    grad = 0;
    for v = 1 : u
        grad = grad + v * nchoosek(u,v)*(v*p^(v-1)*(1-p)^(u-v) - p^v*(u-v)*(1-p)^(u-v-1));
    end
    grad = grad - u^2*(1-p)^(u-1);
    
    %grad = sign(grad);
    
    p_new = p - gamma*grad; % coefficient update
        
    J_old = J; 
    J = costobj(p_new, u);
    
    if (abs(J-J_old) < stopping_th)
        stop = true;
    end
    
    p = p_new;
    iter = iter + 1;
end

fprintf("Number of contending stations: : %d\n", u);
fprintf("Total # of iterations: %d\n", iter);
fprintf("Probability: %.5f\n", p);
fprintf("Objective function: %.3f\n", J);

p = linspace(0,1,100);
Jasd = costobj(p, u);
hold on;
plot(p, Jasd, 'k','linewidth',1)
plot(p_new, J, '-r*','markersize', 12);
hold off;
xlim([0 1]);
ylim([0 10]);
box on;

