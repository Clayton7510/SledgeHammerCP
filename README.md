# SledgeHammerCP
Sledge Hammer • Connection pooling that puts the developer in control 

## Why SledgeHammerCP?
Admittedly, there are a lot of connection pooling options out there: c3p0, Hikari, DBCP, etc. But have you ever had a driver not properly validate connections? Or had a connection leak with no insight into the root cause? Or had a connection pool not enforce its maximum connection limit? These are the problems that SledgeHammerCP solves.

SledgeHammerCP is a simple connection pool manager _[currently based on Apache Commons Pool]_ that focuses on putting the developer control and providing solid connection pool functionality over all else. It's meant to be easily debugged and pluggable in place of other popular connection pool libraries. If you have a problem with connections, SledgeHammerCP can solve it or at the very least highlight where the problem is occurring. In short, it's connection pooling that just works... no fluff, no slick coding tricks, just functional connection pooling.
