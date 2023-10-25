package com.example.onlinebookstore.repository.book;

import com.example.onlinebookstore.exeption.SpecificationNotFoundException;
import com.example.onlinebookstore.model.Book;
import com.example.onlinebookstore.repository.SpecificationProvider;
import com.example.onlinebookstore.repository.SpecificationProviderManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BookSpecificationProviderManager implements SpecificationProviderManager<Book> {
    private final List<SpecificationProvider<Book>> bookSpecificationProviders;

    @Override
    public SpecificationProvider<Book> getSpecificationProvider(String key) {
        return bookSpecificationProviders.stream()
                .filter(b -> b.getKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new SpecificationNotFoundException("Can`t "
                        + "find correct specification provider for key" + key));
    }
}
