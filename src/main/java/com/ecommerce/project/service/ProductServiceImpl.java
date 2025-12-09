package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService{

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${product.image}")
    private String path;

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {

        //check if product is already present

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryId));

        boolean isProductNotPresent = true;
        List<Product> products = category.getProducts();
        for (Product value : products) {
            if (value.getProductName().equals(productDTO.getProductName())) {
                isProductNotPresent = false;
                break;
            }
        }

        Product product = modelMapper.map(productDTO, Product.class);
        if(isProductNotPresent) {
            product.setCategory(category);

            product.setImage("default.png");
            Double specialPrice = product.getPrice() -
                    ((product.getDiscount() * 0.01) * product.getPrice());
            product.setSpecialPrice(specialPrice);


            Product savedProduct = productRepository.save(product);
            return modelMapper.map(savedProduct, ProductDTO.class);
        }else{
            throw new APIException("Product already exists!!!");
        }
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        //products size is 0

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();



        Pageable pageDetails = PageRequest.of(pageNumber,pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findAll(pageDetails);
        List<Product> allProducts = productPage.getContent();



        if(allProducts.isEmpty()){
            throw new APIException("NO products created till now");
        }

        List<ProductDTO> allProductsDTO = allProducts.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();



        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(allProductsDTO);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setLastPage(productResponse.isLastPage());

        return productResponse;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        //products size is 0
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryId));


        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();



        Pageable pageDetails = PageRequest.of(pageNumber,pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findByCategoryOrderByPriceAsc(category,pageDetails);
        List<Product> allProducts = productPage.getContent();


        List<ProductDTO> allProductsDTO = allProducts.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(allProductsDTO);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setLastPage(productResponse.isLastPage());

        return productResponse;

    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        //products size is 0

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber,pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findByProductNameLikeIgnoreCase('%'+keyword+'%',pageDetails);

        List<Product> allProducts = productPage.getContent();

        List<ProductDTO> allProductsDTO = allProducts.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();

        if(allProducts.isEmpty()){
            throw new APIException("No products found with keyword");
        }
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(allProductsDTO);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setLastPage(productResponse.isLastPage());

        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        //get existing product from db
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("Product","productId",productId));

        Product product = modelMapper.map(productDTO,Product.class);
        //update product info with user shared info (one in request body)
        productFromDb.setProductName(product.getProductName());
        productFromDb.setDescription(product.getDescription());
        productFromDb.setQuantity(product.getQuantity());
        productFromDb.setPrice(product.getPrice());
        productFromDb.setDiscount(product.getDiscount());

        Double specialPrice = product.getPrice() -
                ((product.getDiscount()*0.01) * product.getPrice());
        productFromDb.setSpecialPrice(specialPrice);

        Product savedProduct = productRepository.save(productFromDb);


        //save back to db and return saved
        return modelMapper.map(savedProduct,ProductDTO.class);



    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("Product","productId",productId));

        productRepository.deleteById(productId);
        return modelMapper.map(productFromDb,ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {

        // get the product from db
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("Product","productId",productId));

        // upload Image to server
        // Get the filename of uploaded image
        String fileName = fileService.uploadImage(path, image);

        // Updating the new file name to the product
        productFromDb.setImage(fileName);

        //save updated product
        Product updatedProduct = productRepository.save(productFromDb);
        // return DTO after mapping product to DTO
        return modelMapper.map(updatedProduct, ProductDTO.class);

    }


}
