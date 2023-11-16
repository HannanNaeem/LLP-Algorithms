import pykokkos as pk
import numpy as np
import cupy as cp


@pk.workunit
def init_view(tid, view, init_value):
    view[tid] = init_value

@pk.workunit
def llp_stableMarriage(tid, mpref, wrank, sol_view, forbidden_view, dim):
    
    exists_forbidden: int = 1

    while(exists_forbidden):

        # my current woman
        w: int = mpref[tid][sol_view[tid]]

        # check if forbidden - there is a man s.t he ranks her higher(or equal) and so does the woman (strictly higher)
        forbidden_view[tid] = 0
        # if choices are exhausted I am not forbidden
        if sol_view[tid] < dim -1:
            # for man that ranks my woman higher
            for man in range(0, dim):
                for j in range(0, sol_view[tid] + 1):
                    if mpref[man][j] == w and wrank[w][man] < wrank[w][tid]:
                        forbidden_view[tid] = 1
                        

        if forbidden_view[tid]:
            # Advance
            sol_view[tid] = sol_view[tid] + 1


        exists_forbidden = 0
        for i in range(0, dim):
            if forbidden_view[i] == 1:
                exists_forbidden = 1
                break


if __name__ == "__main__":

    mpref = [[0,1,3,2],
             [3,2,0,1],
             [3,1,2,0],
             [2,3,0,1]]
    mpref_copy = mpref.copy()

    wpref = [[2,0,1,3],
             [2,3,0,1],
             [0,3,2,1],
             [1,2,3,0]]
    wpref_copy = wpref.copy()

    cuda_wpref = pk.array(cp.asarray(wpref), pk.CudaSpace, pk.LayoutLeft)
    cuda_mpref = pk.array(cp.asarray(mpref), pk.CudaSpace, pk.LayoutLeft)

    openmp_wpref = pk.array(np.asarray(wpref_copy), pk.HostSpace, pk.LayoutRight)
    openmp_mpref = pk.array(np.asarray(mpref_copy), pk.HostSpace, pk.LayoutRight)

    print(openmp_mpref)
    print(openmp_wpref)
    print(mpref)
    openmp_sol = pk.View([len(mpref)], pk.int32, pk.HostSpace)
    cuda_sol = pk.View([len(mpref)], pk.int32, pk.CudaSpace, pk.LayoutLeft)

    openmp_forbidden = pk.View([len(mpref)], pk.int32, pk.HostSpace)
    cuda_forbidden = pk.View([len(mpref)], pk.int32, pk.CudaSpace, pk.LayoutLeft)

    range_policy_cuda = pk.RangePolicy(pk.ExecutionSpace.Cuda, 0, len(mpref))
    range_policy_openmp = pk.RangePolicy(pk.ExecutionSpace.Default, 0, len(mpref))


    # open mp
    pk.parallel_for(range_policy_openmp, init_view, view=openmp_sol, init_value=0)
    pk.parallel_for(range_policy_openmp, init_view, view=openmp_forbidden, init_value=1)

    print(openmp_sol)

    pk.parallel_for(range_policy_openmp, llp_stableMarriage, mpref=openmp_mpref, wrank=openmp_wpref, sol_view=openmp_sol, forbidden_view=openmp_forbidden, dim=len(mpref))
    print(openmp_sol)