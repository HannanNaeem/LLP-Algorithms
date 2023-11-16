import pykokkos as pk
import numpy as np
import cupy as cp

@pk.workunit
def get_min_neighbors(tid, graph, min_neighbors, dim):
    mw_edge: int = 999999
    mw_neighbor: int = -1
    for i in range(0, dim):
        if graph[tid][i] > 0 and mw_edge > graph[tid][i]:
            mw_edge = graph[tid][i]
            mw_neighbor = i
    min_neighbors[tid] = mw_neighbor
  
@pk.workunit
def init_view(tid, view, init_val):
    view[tid] = init_val
    
@pk.workunit
def init_sol_view(tid, sol_view, min_neighbors):
    # tid is the vertex in sol_view
    same_edge: int = 0
    if tid == min_neighbors[min_neighbors[tid]]:
        same_edge = 1
    
    if not same_edge or same_edge and tid < min_neighbors[tid]:
        sol_view[tid] = min_neighbors[tid]
    else:
        sol_view[tid] = tid
    
@pk.workunit
def llp_boruvka(tid, sol_view, forbidden_view, dim):
    
    exists_forbidden: int = 0
    for i in range(0, dim):
        if forbidden_view[i] == 1:
            exists_forbidden = 1
            break
    
    pk.fence()

    while(exists_forbidden):

        # check if I am forbidden --- read phase
        forbidden_view[tid] = 0
        if sol_view[tid] != sol_view[sol_view[tid]]:
            forbidden_view[tid] = 1
        
        pk.fence()
        # if I am forbidden, advance -- write phase
        if forbidden_view[tid]:
            sol_view[tid] = sol_view[sol_view[tid]]

        exists_forbidden = 0
        for i in range(0, dim):
            if forbidden_view[i] == 1:
                exists_forbidden = 1
                break
        
        pk.fence()

# reduce graph to components and start over, until we have only one vertex
@pk.workunit
def reduce_edges (tid, sol_view, graph, dim):
    # for each edge
    v: int = tid/(dim)
    w: int = tid - (v * (dim))
    
    if sol_view[v] == sol_view[w]:
        graph[v][w] = 0

@pk.workunit
def row_min(tid, mins_view, view2d, dim):
    mins_view[tid] = 999999
    for i in range(0, dim):
        if view2d[tid][i] > 0 and mins_view[tid] > view2d[tid][i]:
            mins_view[tid] = view2d[tid][i]
    if mins_view[tid] == 999999:
        mins_view[tid] = -1

@pk.workunit
def component_min(tid, sol_view, mins_view, component_mins, dim):
    component_mins[tid] = mins_view[tid]

    for i in range(0, dim):
        if sol_view[tid] == sol_view[i] and mins_view[i] > 0 and mins_view[tid] > mins_view[i]:
            # same compenent
            component_mins[tid] = mins_view[i]

@pk.workunit # call for each row
def update_min_neighbours(tid, sol_view, mins_view, component_mins, new_sol_array, dim):
    # for each edge
    pass

@pk.workunit
def new_vertices(tid, acc, sol_view, new_sol_view):
    if sol_view[tid] != tid:
        new_sol_view[tid] = -1
    else:
        acc += 1 

if __name__ == "__main__":

    graph = [[0,7,5,0,0,0,6],
             [7,0,0,9,10,11,0],
             [5,0,0,12,8,0,2],
             [0,9,12,0,0,3,0],
             [0,10,8,0,0,4,0],
             [0,11,0,3,4,0,0],
             [6,0,2,0,0,0,0]]
    
    cp_arr = cp.asarray(graph)
    np_arr = np.asarray(graph)

    cuda_graph = pk.array(cp_arr, pk.CudaSpace, pk.LayoutLeft)
    openmp_graph = pk.array(np_arr, pk.HostSpace, pk.LayoutRight)

    min_tree_openmp = pk.array(cp_arr, pk.CudaSpace, pk.LayoutLeft)
    min_tree_cuda = pk.array(np_arr, pk.HostSpace, pk.LayoutRight)

    forbidden_view_openmp = pk.View([len(graph)], pk.int32, pk.HostSpace)
    forbidden_view_cuda = pk.View([len(graph)], pk.int32, pk.CudaSpace, pk.LayoutLeft)

    min_neighbors_cuda = pk.View([len(graph)], pk.int32, pk.CudaSpace, pk.LayoutLeft)
    min_neighbors_openmp = pk.View([len(graph)], pk.int32, pk.HostSpace, pk.LayoutRight)

    min_weights_cuda = pk.View([len(graph)], pk.int32, pk.CudaSpace, pk.LayoutLeft)
    min_weights_openmp = pk.View([len(graph)], pk.int32, pk.HostSpace, pk.LayoutRight)

    sol_view_openmp = pk.View([len(graph)], pk.int32, pk.HostSpace)
    sol_view_cuda = pk.View([len(graph)], pk.int32, pk.CudaSpace, pk.LayoutLeft)
    
    range_policy_cuda = pk.RangePolicy(pk.ExecutionSpace.Cuda, 0, len(graph))
    range_policy_openmp = pk.RangePolicy(pk.ExecutionSpace.Default, 0, len(graph))


    #### INIT MIN NEIGHBORS
    #openmp
    pk.parallel_for(range_policy_openmp, get_min_neighbors, graph=openmp_graph, min_neighbors=min_neighbors_openmp, dim=len(graph))
    print(min_neighbors_openmp)
    #cuda
    pk.parallel_for(range_policy_cuda, get_min_neighbors, graph=cuda_graph, min_neighbors=min_neighbors_cuda, dim=len(graph))
    print(min_neighbors_cuda)

    #### INIT SOLUTION ARRAY
    #openmp
    pk.parallel_for(range_policy_openmp, init_sol_view, sol_view=sol_view_openmp, min_neighbors=min_neighbors_openmp)
    print(sol_view_openmp)
    #cuda
    pk.parallel_for(range_policy_cuda, init_sol_view, sol_view=sol_view_cuda, min_neighbors=min_neighbors_cuda)
    print(sol_view_cuda)

    #### INIT FORBIDDEN ARRAY
    #openmp
    pk.parallel_for(range_policy_openmp, init_view, view=forbidden_view_openmp, init_val = 1)
    print(forbidden_view_openmp)
    #cuda
    pk.parallel_for(range_policy_cuda, init_view, view=forbidden_view_cuda, init_val = 1)
    print(forbidden_view_cuda)


    ############### OPEN MP --- CPU ##########################

    ## Run LLP instance
    while(True):
        pk.parallel_for(range_policy_openmp, llp_boruvka, sol_view=sol_view_openmp, forbidden_view=forbidden_view_openmp,dim=len(graph))
        new_sol_view = pk.View([len(graph)], pk.int32, pk.HostSpace)
        print(sol_view_openmp)

        # update graph
        new_vertices_count = pk.parallel_reduce(range_policy_openmp, new_vertices, sol_view=sol_view_openmp, new_sol_view=new_sol_view)
        if new_vertices_count <= 1:
            break
        new_min_neighbors = pk.View([len(graph)], pk.int32, pk.HostSpace, pk.LayoutRight)
        new_min_edges = pk.View([len(graph)], pk.int32, pk.HostSpace, pk.LayoutRight)
        component_mins = pk.View([len(graph)], pk.int32, pk.HostSpace, pk.LayoutRight)
        range_policy_edge = pk.RangePolicy(pk.ExecutionSpace.Default, 0, len(graph) * len(graph))
        pk.parallel_for(range_policy_edge, reduce_edges, sol_view=sol_view_openmp, graph=openmp_graph, dim=len(graph))
        pk.parallel_reduce(range_policy_openmp, row_min, mins_view=new_min_edges, view2d=openmp_graph, dim=len(graph))
        pk.parallel_for(range_policy_openmp, component_min, sol_view=sol_view_openmp, mins_view=new_min_edges, component_mins = component_mins, dim = len(graph))
        # get and update min neighbours
        pk.parallel_for(range_policy_openmp, get_min_neighbors, graph=openmp_graph, min_neighbors=new_min_neighbors, dim=len(graph))

        print(new_vertices_count)
        print(openmp_graph)
        print(new_min_neighbors)
        print(component_mins)
        print(new_min_edges)
        print("Picked: 8")
        break

    ## Run Cuda Instance
    while(True):
        pk.parallel_for(range_policy_cuda, llp_boruvka, sol_view=sol_view_cuda, forbidden_view=forbidden_view_cuda,dim=len(graph))
        new_sol_view = pk.View([len(graph)], pk.int32, pk.CudaSpace)
        print(sol_view_cuda)

        # update graph
        new_vertices_count = pk.parallel_reduce(range_policy_cuda, new_vertices, sol_view=sol_view_cuda, new_sol_view=new_sol_view)
        if new_vertices_count <= 1:
            break
        new_min_neighbors = pk.View([len(graph)], pk.int32, pk.CudaSpace, pk.LayoutLeft)
        new_min_edges = pk.View([len(graph)], pk.int32, pk.CudaSpace, pk.LayoutLeft)
        component_mins = pk.View([len(graph)], pk.int32, pk.CudaSpace, pk.LayoutLeft)
        range_policy_edge = pk.RangePolicy(pk.ExecutionSpace.Cuda, 0, len(graph) * len(graph))
        pk.parallel_for(range_policy_edge, reduce_edges, sol_view=sol_view_cuda, graph=cuda_graph, dim=len(graph))
        pk.parallel_reduce(range_policy_cuda, row_min, mins_view=new_min_edges, view2d=cuda_graph, dim=len(graph))
        pk.parallel_for(range_policy_cuda, component_min, sol_view=sol_view_cuda, mins_view=new_min_edges, component_mins = component_mins, dim = len(graph))
        # get and update min neighbours
        pk.parallel_for(range_policy_cuda, get_min_neighbors, graph=cuda_graph, min_neighbors=new_min_neighbors, dim=len(graph))

        print(new_vertices_count)
        # print(cuda_graph)
        print(new_min_neighbors)
        print(component_mins)
        print(new_min_edges)
        print("Picked: 8")
        break   
   