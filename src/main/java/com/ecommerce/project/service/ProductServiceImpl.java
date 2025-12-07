package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService{

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryId));



        productDTO.setCategory(category);

        productDTO.setImage("default.png");
        Double specialPrice = productDTO.getPrice() -
                ((productDTO.getDiscount()*0.01) * productDTO.getPrice());
        productDTO.setSpecialPrice(specialPrice);

        Product product = modelMapper.map(productDTO,Product.class);

        Product savedProduct = productRepository.save(product);
        return modelMapper.map(savedProduct,ProductDTO.class);
    }

    @Override
    public ProductResponse getAllProducts() {
        List<Product> allProducts = productRepository.findAll();
        List<ProductDTO> allProductsDTO = allProducts.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(allProductsDTO);

        return productResponse;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryId));

        List<Product> allProducts = productRepository.findByCategoryOrderByPriceAsc(category);

        List<ProductDTO> allProductsDTO = allProducts.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(allProductsDTO);

        return productResponse;

    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword) {

        List<Product> allProducts = productRepository.findByProductNameLikeIgnoreCase('%'+keyword+'%');

        List<ProductDTO> allProductsDTO = allProducts.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(allProductsDTO);

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
}
