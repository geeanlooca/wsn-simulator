function [ collision ] = CONTI( n,k,p )
%CONTI tests wether a collision occurs when employing the CONTI protocol
%   Implementation of Algorithm 1 in the 'A Medium Access Control Scheme for Wireless
%   LANs with Constant-Time Contention' by Zakhia Abichar and J. Morris Chang

retired = 0;

for i = 1 : k
    contending = n - retired;
    signals = rand(1, contending) <= p(i);
    listening = contending - sum(signals);    
    if (listening ~= contending)
        retired = retired + listening;
    end
end

if (n-retired)> 1
    collision = 1;
else
    collision = 0;
end

end

