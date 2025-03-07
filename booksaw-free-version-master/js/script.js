(function ($) {

  "use strict";

  const tabs = document.querySelectorAll('[data-tab-target]')
  const tabContents = document.querySelectorAll('[data-tab-content]')

  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      const target = document.querySelector(tab.dataset.tabTarget)
      tabContents.forEach(tabContent => {
        tabContent.classList.remove('active')
      })
      tabs.forEach(tab => {
        tab.classList.remove('active')
      })
      tab.classList.add('active')
      target.classList.add('active')
    })
  });

  // Populate modal for each book
  const books = document.querySelectorAll(".product-item");

  books.forEach(book => {
    book.addEventListener("click", function () {
      // Get book details from data attributes
      let title = this.getAttribute("data-title");
      let author = this.getAttribute("data-author");
      let price = this.getAttribute("data-price");
      let image = this.getAttribute("data-image");
      let description = this.getAttribute("data-description");

      // Update modal content
      document.getElementById("bookModalLabel").textContent = title;
      document.getElementById("bookImage").src = image;
      document.getElementById("bookAuthor").innerHTML = `<strong>Author: </strong>${author}`
      document.getElementById("bookPrice").innerHTML = `<strong>Price: </strong>${price}`;
      document.getElementById("bookDescription").innerHTML = `<strong>Description: </strong>${description}`;

      // Hide the read more button
      //this.style.display = "none";
    });
  });

  // Responsive Navigation with Button
  const hamburger = document.querySelector(".hamburger");
  const navMenu = document.querySelector(".menu-list");

  hamburger.addEventListener("click", mobileMenu);

  function mobileMenu() {
    hamburger.classList.toggle("active");
    navMenu.classList.toggle("responsive");
  }

  const navLink = document.querySelectorAll(".nav-link");

  navLink.forEach(n => n.addEventListener("click", closeMenu));

  function closeMenu() {
    hamburger.classList.remove("active");
    navMenu.classList.remove("responsive");
  }

  var initScrollNav = function () {
    var scroll = $(window).scrollTop();

    if (scroll >= 200) {
      $('#header-wrap').addClass("fixed-top");
    } else {
      $('#header-wrap').removeClass("fixed-top");
    }
  }

  $(window).scroll(function () {
    initScrollNav();
  });

  $(document).ready(function () {
    initScrollNav();

    Chocolat(document.querySelectorAll('.image-link'), {
      imageSize: 'contain',
      loop: true,
    })

    $('#header-wrap').on('click', '.search-toggle', function (e) {
      var selector = $(this).data('selector');

      $(selector).toggleClass('show').find('.search-input').focus();
      $(this).toggleClass('active');

      e.preventDefault();
    });


    // close when click off of container
    $(document).on('click touchstart', function (e) {
      if (!$(e.target).is('.search-toggle, .search-toggle *, #header-wrap, #header-wrap *')) {
        $('.search-toggle').removeClass('active');
        $('#header-wrap').removeClass('show');
      }
    });

    $('.main-slider').slick({
      autoplay: false,
      autoplaySpeed: 4000,
      fade: true,
      dots: true,
      prevArrow: $('.prev'),
      nextArrow: $('.next'),
    });

    $('.product-grid').slick({
      slidesToShow: 4,
      slidesToScroll: 1,
      autoplay: false,
      autoplaySpeed: 2000,
      dots: true,
      arrows: false,
      responsive: [
        {
          breakpoint: 1400,
          settings: {
            slidesToShow: 3,
            slidesToScroll: 1
          }
        },
        {
          breakpoint: 999,
          settings: {
            slidesToShow: 2,
            slidesToScroll: 1
          }
        },
        {
          breakpoint: 660,
          settings: {
            slidesToShow: 1,
            slidesToScroll: 1
          }
        }
        // You can unslick at a given breakpoint now by adding:
        // settings: "unslick"
        // instead of a settings object
      ]
    });

    AOS.init({
      duration: 1200,
      once: true,
    })

    jQuery('.stellarnav').stellarNav({
      theme: 'plain',
      closingDelay: 250,
      // mobileMode: false,
    });

  }); // End of a document


})(jQuery);