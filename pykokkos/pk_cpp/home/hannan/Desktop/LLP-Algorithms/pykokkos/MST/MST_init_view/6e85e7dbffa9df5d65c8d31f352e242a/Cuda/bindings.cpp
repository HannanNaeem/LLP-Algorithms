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

void run_init_view(int32_t init_val,Kokkos::View<int32_t*,Kokkos::LayoutLeft,pk_arg_memspace,Kokkos::Experimental::DefaultViewHooks> view,pk_exec_space pk_exec_space_instance,const std::string& pk_kernel_name,int pk_threads_begin,int pk_threads_end,int pk_randpool_seed,int pk_randpool_num_states) {  auto pk_d_view = Kokkos::create_mirror_view_and_copy(pk_exec_space_instance, view);pk_functor_init_view<pk_exec_space> pk_f(init_val,pk_d_view,pk_randpool_seed,pk_randpool_num_states); Kokkos::parallel_for(pk_kernel_name,Kokkos::RangePolicy<pk_exec_space,pk_functor_init_view<pk_exec_space>::init_view_tag>(pk_exec_space_instance, pk_threads_begin,pk_threads_end),pk_f);Kokkos::resize(view,pk_d_view.extent(0));Kokkos::deep_copy(view, pk_d_view); }
void wrapper_init_view(pybind11::kwargs kwargs) {run_init_view(kwargs["init_val"].cast<int32_t>(),kwargs["view"].cast<Kokkos::View<int32_t*,Kokkos::LayoutLeft,pk_arg_memspace,Kokkos::Experimental::DefaultViewHooks>>(),kwargs["pk_exec_space_instance"].cast<pk_exec_space>(),kwargs["pk_kernel_name"].cast<std::string>(),kwargs["pk_threads_begin"].cast<int>(),kwargs["pk_threads_end"].cast<int>(),kwargs["pk_randpool_seed"].cast<int>(),kwargs["pk_randpool_num_states"].cast<int>());;}
PYBIND11_MODULE(pk_cpp_home_hannan_Desktop_LLP_Algorithms_pykokkos_MST_MST_init_view_6e85e7dbffa9df5d65c8d31f352e242a_Cuda_kernel_cpython_311_x86_64_linux_gnu_so, k) {k.def("wrapper_init_view", &wrapper_init_view);}