function [ F ] = DCF_system(x,n)
%UNTITLED3 Summary of this function goes here
%   Detailed explanation goes here
CWmin = 16;
CWmax = 1024;
m = log2(CWmax/CWmin);

% x(1) = p
% x(2) = tau
F(1) = 1 - (1-x(2)).^(n-1) - x(1); % p
F(2) = 2./(1 + CWmin + x(1).*CWmin .* sum( (2*x(1)).^(0:m-1))) - x(2); % tau

end

