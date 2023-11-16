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

double run_new_vertices(Kokkos::View<int32_t*,Kokkos::LayoutLeft,pk_arg_memspace,Kokkos::Experimental::DefaultViewHooks> sol_view,Kokkos::View<int32_t*,Kokkos::LayoutLeft,pk_arg_memspace,Kokkos::Experimental::DefaultViewHooks> new_sol_view,pk_exec_space pk_exec_space_instance,const std::string& pk_kernel_name,int pk_threads_begin,int pk_threads_end,int pk_randpool_seed,int pk_randpool_num_states) { double pk_acc = 0; auto pk_d_sol_view = Kokkos::create_mirror_view_and_copy(pk_exec_space_instance, sol_view);auto pk_d_new_sol_view = Kokkos::create_mirror_view_and_copy(pk_exec_space_instance, new_sol_view);pk_functor_new_vertices<pk_exec_space> pk_f(pk_d_sol_view,pk_d_new_sol_view,pk_randpool_seed,pk_randpool_num_states); Kokkos::parallel_reduce(pk_kernel_name,Kokkos::RangePolicy<pk_exec_space,pk_functor_new_vertices<pk_exec_space>::new_vertices_tag>(pk_exec_space_instance, pk_threads_begin,pk_threads_end),pk_f,pk_acc);Kokkos::resize(sol_view,pk_d_sol_view.extent(0));Kokkos::deep_copy(sol_view, pk_d_sol_view);Kokkos::resize(new_sol_view,pk_d_new_sol_view.extent(0));Kokkos::deep_copy(new_sol_view, pk_d_new_sol_view);return pk_acc; }
double wrapper_new_vertices(pybind11::kwargs kwargs) {return run_new_vertices(kwargs["sol_view"].cast<Kokkos::View<int32_t*,Kokkos::LayoutLeft,pk_arg_memspace,Kokkos::Experimental::DefaultViewHooks>>(),kwargs["new_sol_view"].cast<Kokkos::View<int32_t*,Kokkos::LayoutLeft,pk_arg_memspace,Kokkos::Experimental::DefaultViewHooks>>(),kwargs["pk_exec_space_instance"].cast<pk_exec_space>(),kwargs["pk_kernel_name"].cast<std::string>(),kwargs["pk_threads_begin"].cast<int>(),kwargs["pk_threads_end"].cast<int>(),kwargs["pk_randpool_seed"].cast<int>(),kwargs["pk_randpool_num_states"].cast<int>());;}
PYBIND11_MODULE(pk_cpp_home_hannan_Desktop_LLP_Algorithms_pykokkos_MST_MST_new_vertices_35c7af7835c64662fc0ac345adfec400_Cuda_kernel_cpython_311_x86_64_linux_gnu_so, k) {k.def("wrapper_new_vertices", &wrapper_new_vertices);}