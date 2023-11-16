// ******* AUTOMATICALLY GENERATED BY PyKokkos *******
#include <pybind11/pybind11.h>
#include <Kokkos_Core.hpp>
#include <Kokkos_Random.hpp>
#include <Kokkos_Sort.hpp>
#include <fstream>
#include <iostream>
#include <cmath>
#include <functor.hpp>
#include <functor_cast.hpp>

void run_get_min_neighbors(int32_t dim,Kokkos::View<int64_t**,Kokkos::LayoutRight,Kokkos::CudaSpace,Kokkos::Experimental::DefaultViewHooks> graph,Kokkos::View<int32_t*,Kokkos::LayoutLeft,pk_arg_memspace,Kokkos::Experimental::DefaultViewHooks> min_neighbors,pk_exec_space pk_exec_space_instance,const std::string& pk_kernel_name,int pk_threads_begin,int pk_threads_end,int pk_randpool_seed,int pk_randpool_num_states) {  auto pk_d_graph = graph;auto pk_d_min_neighbors = Kokkos::create_mirror_view_and_copy(pk_exec_space_instance, min_neighbors);pk_functor_get_min_neighbors<pk_exec_space> pk_f(dim,pk_d_graph,pk_d_min_neighbors,pk_randpool_seed,pk_randpool_num_states); Kokkos::parallel_for(pk_kernel_name,Kokkos::RangePolicy<pk_exec_space,pk_functor_get_min_neighbors<pk_exec_space>::get_min_neighbors_tag>(pk_exec_space_instance, pk_threads_begin,pk_threads_end),pk_f);Kokkos::resize(min_neighbors,pk_d_min_neighbors.extent(0));Kokkos::deep_copy(min_neighbors, pk_d_min_neighbors); }
void wrapper_get_min_neighbors(pybind11::kwargs kwargs) {run_get_min_neighbors(kwargs["dim"].cast<int32_t>(),kwargs["graph"].cast<Kokkos::View<int64_t**,Kokkos::LayoutRight,Kokkos::CudaSpace,Kokkos::Experimental::DefaultViewHooks>>(),kwargs["min_neighbors"].cast<Kokkos::View<int32_t*,Kokkos::LayoutLeft,pk_arg_memspace,Kokkos::Experimental::DefaultViewHooks>>(),kwargs["pk_exec_space_instance"].cast<pk_exec_space>(),kwargs["pk_kernel_name"].cast<std::string>(),kwargs["pk_threads_begin"].cast<int>(),kwargs["pk_threads_end"].cast<int>(),kwargs["pk_randpool_seed"].cast<int>(),kwargs["pk_randpool_num_states"].cast<int>());;}
PYBIND11_MODULE(pk_cpp_home_hannan_Desktop_LLP_Algorithms_pykokkos_MST_MST_get_min_neighbors_44c479332562af390665e03e04029d01_Cuda_kernel_cpython_311_x86_64_linux_gnu_so, k) {k.def("wrapper_get_min_neighbors", &wrapper_get_min_neighbors);}